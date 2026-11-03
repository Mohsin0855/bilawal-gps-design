package com.example.gpsmapcamera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.gpsmapcamera.R

class TemplatesBottomSheetFragment : BottomSheetDialogFragment() {

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

        val template1 = view.findViewById<ConstraintLayout>(R.id.template1)
        val template2 = view.findViewById<ConstraintLayout>(R.id.template2)
        val template3 = view.findViewById<ConstraintLayout>(R.id.template3)
        val template4 = view.findViewById<ConstraintLayout>(R.id.template4)

        val allTemplates = listOf(template1, template2, template3, template4)

        updateTemplateSelection(allTemplates, selectedTemplateId)

        template1.setOnClickListener { selectAndNotify(allTemplates, template1.id) }
        template2.setOnClickListener { selectAndNotify(allTemplates, template2.id) }
        template3.setOnClickListener { selectAndNotify(allTemplates, template3.id) }
        template4.setOnClickListener { selectAndNotify(allTemplates, template4.id) }

        val closeButton = view.findViewById<View>(R.id.smallclose)
        closeButton?.setOnClickListener {
            dismiss()
        }
    }

    private fun selectAndNotify(templates: List<ConstraintLayout>, clickedId: Int) {
        updateTemplateSelection(templates, clickedId)

        val destinationFragmentId = when (clickedId) {
            R.id.template1 -> R.id.template1Fragment
            R.id.template2 -> R.id.template2Fragment
            R.id.template3 -> R.id.template3Fragment
            R.id.template4 -> R.id.template4Fragment
            else -> R.id.template1Fragment
        }

        setFragmentResult("templateRequestKey", bundleOf("selectedTemplateKey" to destinationFragmentId))

    }

    private fun updateTemplateSelection(templates: List<ConstraintLayout>, clickedId: Int) {
        selectedTemplateId = clickedId

        templates.forEach { template ->
            val tickIcon = template.getChildAt(1)
            val editIcon = template.getChildAt(2)

            if (template.id == clickedId) {
                template.setBackgroundResource(R.drawable.selected_template)
                tickIcon?.visibility = View.VISIBLE
                editIcon?.visibility = View.VISIBLE
            } else {
                template.setBackgroundResource(R.drawable.unselected_template)
                tickIcon?.visibility = View.GONE
                editIcon?.visibility = View.GONE
            }
        }
    }
}