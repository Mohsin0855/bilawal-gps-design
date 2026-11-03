package com.example.gpsmapcamera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.gpsmapcamera.R

class Fragment_bottomsheet : BottomSheetDialogFragment() {

    private var selectedTemplateId: Int = R.id.template1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_templates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedTemplateId = arguments?.getInt("CURRENT_SELECTED_TEMPLATE", R.id.template1) ?: R.id.template1

        val template1 = view.findViewById<ConstraintLayout>(R.id.template1)
        val template2 = view.findViewById<ConstraintLayout>(R.id.template2)
        val template3 = view.findViewById<ConstraintLayout>(R.id.template3)
        val template4 = view.findViewById<ConstraintLayout>(R.id.template4)

        val allTemplates = listOf(template1, template2, template3, template4)
        updateTemplateSelection(view, allTemplates, selectedTemplateId)

        template1.setOnClickListener { selectAndNotify(view, allTemplates, template1.id) }
        template2.setOnClickListener { selectAndNotify(view, allTemplates, template2.id) }
        template3.setOnClickListener { selectAndNotify(view, allTemplates, template3.id) }
        template4.setOnClickListener { selectAndNotify(view, allTemplates, template4.id) }

        val closeButton = view.findViewById<View>(R.id.smallclose)
        closeButton?.setOnClickListener {
            dismiss()
        }
    }

    private fun selectAndNotify(rootView: View, templates: List<ConstraintLayout>, clickedId: Int) {
        updateTemplateSelection(rootView, templates, clickedId)
        setFragmentResult("templateRequestKey", bundleOf("selectedTemplateKey" to clickedId))
    }

    private fun updateTemplateSelection(rootView: View, templates: List<ConstraintLayout>, clickedId: Int) {
        selectedTemplateId = clickedId

        templates.forEach { template ->
            val (tickId, editId) = when (template.id) {
                R.id.template1 -> Pair(R.id.template1_tick, R.id.template1_edit)
                R.id.template2 -> Pair(R.id.template2_tick, R.id.template2_edit)
                R.id.template3 -> Pair(R.id.template3_tick, R.id.template3_edit)
                R.id.template4 -> Pair(R.id.template4_tick, R.id.template4_edit)
                else -> Pair(0, 0)
            }

            val tickIcon = rootView.findViewById<ImageView>(tickId)
            val editIcon = rootView.findViewById<ImageView>(editId)

            if (template.id == clickedId) {
                template.setBackgroundResource(R.drawable.selected_template)
                tickIcon?.visibility = View.VISIBLE
                editIcon?.visibility = View.VISIBLE
            } else {
                template.setBackgroundResource(R.drawable.unselected_template)
                tickIcon?.visibility = View.GONE
                editIcon?.visibility = View.GONE
            }
            template.invalidate()
        }
    }
}