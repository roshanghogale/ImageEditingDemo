package com.learnteachsalefromads.imageeditingdemo.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.learnteachsalefromads.imageeditingdemo.R
import com.learnteachsalefromads.imageeditingdemo.adapters.LayerAdapter
import com.learnteachsalefromads.imageeditingdemo.adapters.ToolAdapter
import com.learnteachsalefromads.imageeditingdemo.controllers.CanvasController
import com.learnteachsalefromads.imageeditingdemo.controllers.LayerInteractionController
import com.learnteachsalefromads.imageeditingdemo.controllers.ToolController
import com.learnteachsalefromads.imageeditingdemo.editor.EditorContext
import com.learnteachsalefromads.imageeditingdemo.editor.actions.AddLayerAction
import com.learnteachsalefromads.imageeditingdemo.layer.LayerFactory
import com.learnteachsalefromads.imageeditingdemo.layer.LayerManager
import com.learnteachsalefromads.imageeditingdemo.models.ToolAction
import com.learnteachsalefromads.imageeditingdemo.models.ToolItem

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var canvasLayout: FrameLayout
    private lateinit var btnAddLayerInline: ImageView
    private lateinit var layerRecycler: RecyclerView
    private lateinit var toolRecycler: RecyclerView

    private lateinit var layerManager: LayerManager
    private lateinit var layerAdapter: LayerAdapter
    private lateinit var toolAdapter: ToolAdapter

    private lateinit var canvasController: CanvasController
    private lateinit var layerInteractionController: LayerInteractionController
    private lateinit var toolController: ToolController

    private val layerFactory = LayerFactory()

    override fun onCreate(savedInstanceState: Bundle?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupInsets()
        setupManagers()
        setupRecyclerViews()
        setupControllers()
        attachDragAndDrop()
        setupClicks()
        updateToolbarState()
    }

    private fun bindViews() {
        rootLayout = findViewById(R.id.rootLayout)
        canvasLayout = findViewById(R.id.canvasLayout)
        btnAddLayerInline = findViewById(R.id.btnAddLayerInline)
        layerRecycler = findViewById(R.id.layerRecycler)
        toolRecycler = findViewById(R.id.toolRecycler)
    }

    private fun setupInsets() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rootLayout.setOnApplyWindowInsetsListener { v, insets ->
                val b = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(b.left, b.top, b.right, b.bottom)
                insets
            }
        }
    }

    private fun setupManagers() {
        layerManager = LayerManager(canvasLayout)
    }

    private fun setupRecyclerViews() {

        layerAdapter = LayerAdapter(layerManager) { index ->
            layerInteractionController.onLayerClicked(index)
            updateVisibilityToolIcon()
        }

        layerRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        layerRecycler.adapter = layerAdapter

        toolAdapter = ToolAdapter(createTools()) {
            toolController.onToolAction(it)
        }

        toolRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        toolRecycler.adapter = toolAdapter
    }

    private fun setupControllers() {

        layerInteractionController =
            LayerInteractionController(layerManager, layerAdapter, layerRecycler)

        toolController =
            ToolController(
                activity = this,
                layerManager = layerManager,
                layerAdapter = layerAdapter,
                onStateChanged = { updateToolbarState() }
            )

        canvasController = CanvasController(canvasLayout, layerManager)
        canvasController.attach()
    }

    private fun setupClicks() {
        btnAddLayerInline.setOnClickListener { checkAndRequestPermissions() }
    }

    /* ---------- TOOLBAR ---------- */

    private fun updateToolbarState() {
        if (layerManager.layers.isEmpty()) {
            toolRecycler.visibility = View.GONE
            return
        }

        toolRecycler.visibility = View.VISIBLE
        updateVisibilityToolIcon()
    }

    private fun updateVisibilityToolIcon() {
        val index = layerManager.selectedIndex
        if (index !in layerManager.layers.indices) return
        toolAdapter.updateVisibilityTool(layerManager.layers[index].isVisible)
    }

    private fun createTools() = mutableListOf(
        ToolItem(ToolAction.UNDO, R.drawable.ic_undo, "Undo"),
        ToolItem(ToolAction.REDO, R.drawable.ic_redo, "Redo"),
        ToolItem(ToolAction.TOGGLE_VISIBILITY, R.drawable.ic_eye_closed, "Hide"),
        ToolItem(ToolAction.ROTATE, R.drawable.ic_rotate, "Rotate"),
        ToolItem(ToolAction.DUPLICATE, R.drawable.ic_duplicate, "Duplicate"),
        ToolItem(ToolAction.DELETE, R.drawable.ic_delete, "Delete")
    )

    /* ---------- DRAG & DROP ---------- */

    private fun attachDragAndDrop() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
        ) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                layerAdapter.moveItem(vh.adapterPosition, target.adapterPosition)
                return true
            }

            override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
                super.clearView(rv, vh)
                layerAdapter.selectAfterDrop(vh.adapterPosition)
                updateToolbarState()
            }

            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {}
        }).attachToRecyclerView(layerRecycler)
    }

    /* ---------- ADD IMAGE ---------- */

    private fun addImageLayer(uri: Uri) {

        val layer = layerFactory.createImageLayer(
            canvasLayout,
            uri,
            "Layer ${layerManager.layers.size + 1}"
        )

        EditorContext.undoRedoManager.push(
            AddLayerAction(layer, layerManager.layers, layerManager.layers.size)
        )

        layerManager.add(layer)
        layerAdapter.notifyDataSetChanged()
        updateToolbarState()
    }

    /* ---------- PERMISSIONS ---------- */

    private fun checkAndRequestPermissions() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                ) openGallery()
                else permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                if (Environment.isExternalStorageManager()) openGallery()
                else startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        .setData(Uri.parse("package:$packageName"))
                )

            else ->
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) openGallery()
                else permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) openGallery()
            else Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show()
        }

    private fun openGallery() {
        galleryLauncher.launch(Intent(Intent.ACTION_PICK).apply { type = "image/*" })
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK)
                it.data?.data?.let(::addImageLayer)
        }
}
