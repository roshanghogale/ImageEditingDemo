package com.learnteachsalefromads.imageeditingdemo.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
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
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem
import com.learnteachsalefromads.imageeditingdemo.models.ToolAction
import com.learnteachsalefromads.imageeditingdemo.models.ToolItem
import com.learnteachsalefromads.imageeditingdemo.utils.CanvasGestureController
import com.learnteachsalefromads.imageeditingdemo.utils.LayerManager
import com.learnteachsalefromads.imageeditingdemo.utils.RotateBottomSheet

class MainActivity : AppCompatActivity() {

    /* ================= UI ================= */

    private lateinit var rootLayout: LinearLayout
    private lateinit var canvasLayout: FrameLayout
    private lateinit var btnAddLayerInline: ImageView
    private lateinit var layerRecycler: RecyclerView
    private lateinit var toolRecycler: RecyclerView

    /* ================= STATE ================= */

    private var lastClickedLayerIndex = -1

    /* ================= SYSTEM ================= */

    private lateinit var layerManager: LayerManager
    private lateinit var layerAdapter: LayerAdapter
    private lateinit var toolAdapter: ToolAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* ---------- Insets ---------- */
        rootLayout = findViewById(R.id.rootLayout)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rootLayout.setOnApplyWindowInsetsListener { v, insets ->
                val b = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(b.left, b.top, b.right, b.bottom)
                insets
            }
        }

        /* ---------- Bind ---------- */
        canvasLayout = findViewById(R.id.canvasLayout)
        btnAddLayerInline = findViewById(R.id.btnAddLayerInline)
        layerRecycler = findViewById(R.id.layerRecycler)
        toolRecycler = findViewById(R.id.toolRecycler)

        layerManager = LayerManager(canvasLayout)

        /* ---------- Layers ---------- */
        layerAdapter = LayerAdapter(layerManager) { index ->
            handleLayerClick(index)
        }

        layerRecycler.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = layerAdapter
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        /* ---------- Tools ---------- */
        toolAdapter = ToolAdapter(createTools(), ::handleToolAction)

        toolRecycler.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = toolAdapter

            // 🚀 IMPORTANT: Disable animations to prevent icon fade
            itemAnimator = null

            visibility = View.GONE
        }

        attachDragAndDrop()

        btnAddLayerInline.setOnClickListener {
            checkAndRequestPermissions()
        }

        canvasLayout.setOnClickListener {
            hideTools()
            lastClickedLayerIndex = -1
        }

        attachCanvasGestures()
    }

    private fun attachCanvasGestures() {

        val gesture = CanvasGestureController(
            this,
            { layerManager.layers.getOrNull(layerManager.selectedIndex)?.container }
        )

        canvasLayout.setOnTouchListener { _, event ->
            gesture.onTouch(event)
            true
        }
    }


    /* ================= Layer Click ================= */

    private fun handleLayerClick(index: Int) {
        if (layerManager.selectedIndex == index && lastClickedLayerIndex == index) {
            toggleTools()
        } else {
            hideTools()
            layerManager.select(index)
        }

        lastClickedLayerIndex = index
        layerAdapter.notifyDataSetChanged()
        scrollToSelected()
        updateVisibilityToolIcon()
    }

    private fun toggleTools() {
        toolRecycler.visibility =
            if (toolRecycler.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun hideTools() {
        toolRecycler.visibility = View.GONE
    }

    private fun scrollToSelected() {
        val pos = layerManager.adapterPositionForSelected()
        if (pos != -1) layerRecycler.scrollToPosition(pos)
    }

    /* ================= Tools ================= */

    private fun createTools() = mutableListOf(
        ToolItem(ToolAction.TOGGLE_VISIBILITY, R.drawable.ic_eye_closed, "Visibility"),
        ToolItem(ToolAction.ROTATE, R.drawable.ic_rotate, "Rotate"), // ✅ NEW
        ToolItem(ToolAction.DUPLICATE, R.drawable.ic_duplicate, "Duplicate"),
        ToolItem(ToolAction.DELETE, R.drawable.ic_delete, "Delete")
    )

    private fun updateVisibilityToolIcon() {
        val index = layerManager.selectedIndex
        if (index == -1) return

        toolAdapter.updateVisibilityTool(
            layerManager.layers[index].isVisible
        )
    }

    private fun handleToolAction(tool: ToolItem) {
        val index = layerManager.selectedIndex
        if (index == -1) return

        when (tool.id) {

            ToolAction.ROTATE -> {
                RotateBottomSheet {
                    layerManager.layers.getOrNull(index)?.container
                }.show(supportFragmentManager, "rotate")
            }

            ToolAction.TOGGLE_VISIBILITY -> {
                layerManager.toggleVisibility(index)
                updateVisibilityToolIcon()
            }

            ToolAction.DUPLICATE -> layerManager.duplicate(index)
            ToolAction.DELETE -> layerManager.remove(index)
            ToolAction.LOCK -> Unit
        }

        hideTools()
    }


    /* ================= Drag & Drop ================= */

    private fun attachDragAndDrop() {
        ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
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
                    hideTools()
                }

                override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {}
            }
        ).attachToRecyclerView(layerRecycler)
    }

    /* ================= Add Image ================= */

    // unchanged EXCEPT addImageLayer()
    private fun addImageLayer(uri: Uri) {

        val image = ImageView(this).apply {
            setImageURI(uri)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val rotateHandle = ImageView(this).apply {
            setImageResource(R.drawable.ic_rotate)
            layoutParams = FrameLayout.LayoutParams(48, 48).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                topMargin = -48
            }
            visibility = View.GONE
        }

        val container = FrameLayout(this).apply {
            clipChildren = false
            clipToPadding = false

            addView(
                image,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )

            addView(rotateHandle)
        }

        layerManager.add(
            LayerItem(
                name = "Layer ${layerManager.layers.size + 1}",
                container = container,
                imageView = image,
                rotateHandle = rotateHandle
            )
        )

        layerAdapter.notifyDataSetChanged()
    }

    /* ================= Permissions ================= */

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
