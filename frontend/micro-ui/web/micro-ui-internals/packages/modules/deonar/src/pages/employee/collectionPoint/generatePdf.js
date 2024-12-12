import React, { Fragment, useEffect, useMemo, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Controller, useForm } from "react-hook-form";
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

// Add these utility functions for PDF generation
const generatePDF = async (element, filename = 'fee_confirmation.pdf') => {
  try {
    // Capture the element as a canvas
    const canvas = await html2canvas(element, { 
      scale: 2, 
      useCORS: true,
      logging: false 
    });
    
    // Create PDF with jsPDF
    const pdf = new jsPDF('p', 'mm', 'a4');
    const imgData = canvas.toDataURL('image/png');
    const imgProps = pdf.getImageProperties(imgData);
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = (imgProps.height * pdfWidth) / imgProps.width;

    pdf.addImage(imgData, 'PNG', 0, 0, pdfWidth, pdfHeight);
    pdf.save(filename);
  } catch (error) {
    console.error('PDF Generation Error:', error);
    // Consider showing a toast or error message to the user
  }
};

export default generatePDF;