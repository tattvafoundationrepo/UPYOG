// import React, { useState } from "react";
// import { useTranslation } from "react-i18next";

// const SubmitPrintButtonFields = ({ submit, print, isValid }) => {
//   const { t } = useTranslation();
//   const [isSaved, setIsSaved] = useState(false);

//   const handleSaveClick = () => {
//     if (isValid) {
//       setIsSaved(true);
//       // submit();
//     }
//   };

//   const handlePrintClick = () => {
//     // Ensure the 'gatePassContent' element exists and contains the correct HTML
//     const printContent = document.getElementById("gatePassContent")?.innerHTML;

//     if (printContent) {
//       const newWindow = window.open("", "", "height=600,width=800");
//       newWindow.document.write("<html><head><title>" + t("DEONAR_GATE_PASS") + "</title>");
//       newWindow.document.write("<style>body { font-family: Arial, sans-serif; margin: 20px; }</style>");
//       newWindow.document.write("</head><body>");
//       newWindow.document.write(printContent);
//       newWindow.document.write("</body></html>");
//       newWindow.document.close();
//       newWindow.print();
//     } else {
//       console.error("No content found to print.");
//     }
//   };

//   return (
//     <div className="bmc-card-row">
//       <div style={{ float: "right", paddingBottom: "1rem", textAlign: "end" }}>
//         <button
//           type="button"
//           className="bmc-card-button"
//           style={{
//             marginRight: "1rem",
//             borderBottom: "3px solid black",
//             backgroundColor: isSaved ? "#f47738" : "gray",
//           }}
//           disabled={!isSaved}
//           onClick={handlePrintClick}
//         >
//           {t("BMC_PRINT")}
//         </button>
//         <button
//           type="submit"
//           className="bmc-card-button"
//           style={{ marginRight: "1rem", borderBottom: "3px solid black", backgroundColor: isValid ? "#f47738" : "gray" }}
//           disabled={!isValid}
//           onClick={handleSaveClick}
//         >
//           {t("BMC_SAVE")}
//         </button>
//       </div>
//     </div>
//   );
// };

// export default SubmitPrintButtonFields;

// import React, { useState } from "react";
// import { useTranslation } from "react-i18next";

// const SubmitPrintButtonFields = ({ submit, print, isValid }) => {
//   const { t } = useTranslation();
//   const [isSaved, setIsSaved] = useState(false);

//   const handleSaveClick = () => {
//     if (isValid) {
//       setIsSaved(true);
//       // submit();
//     }
//   };

//   const handlePrintClick = () => {
//     const printContent = document.getElementById("gatePassContent")?.innerHTML;

//     if (printContent) {
//       const newWindow = window.open("", "", "height=600,width=800");
//       const contentWithoutPagination = printContent.replace(/<div[^>]*class="pagination dss-white-pre"[^>]*>[\s\S]*?<\/div>/g, "");
//       newWindow.document.write("<html><head><title>" + t("DEONAR_GATE_PASS") + "</title>");

//       newWindow.document.write(`
//         <style>
//           body {
//             font-family: Arial, sans-serif;
//             margin: 20px;
//             color: #333;
//           }
//           h2 {
//             text-align: center;
//             font-size: 24px;
//             margin: 0;
//           }
//           p {
//             text-align: center;
//             font-size: 16px;
//             margin: 10px 0;
//           }
//           .bmc-card-row {
//             margin: 15px 0;
//           }
//           table {
//             width: 100%;
//             border-collapse: collapse;
//             margin-top: 20px;
//             margin-bottom: 20px;
//           }
//           table, th, td {
//             border: 1px solid #ddd;
//           }
//           th, td {
//             padding: 8px;
//             text-align: left;
//           }
//           th {
//             background-color: #f4f4f4;
//           }
//           .total {
//             font-weight: bold;
//             font-size: 18px;
//           }
//         </style>
//       `);

//       newWindow.document.write("</head><body>");
//       newWindow.document.write(contentWithoutPagination);
//       newWindow.document.write("</body></html>");
//       newWindow.document.close();
//       newWindow.print();
//     }
//   };

//   return (
//     <div className="bmc-card-row">
//       <div style={{ float: "right", paddingBottom: "1rem", textAlign: "end" }}>
//         <button
//           type="button"
//           className="bmc-card-button"
//           style={{
//             marginRight: "1rem",
//             borderBottom: "3px solid black",
//             backgroundColor: isSaved ? "#f47738" : "gray",
//           }}
//           disabled={!isSaved}
//           onClick={handlePrintClick}
//         >
//           {t("Deonar_PRINT")}
//         </button>
//         <button
//           type="submit"
//           className="bmc-card-button"
//           style={{ marginRight: "1rem", borderBottom: "3px solid black", backgroundColor: isValid ? "#f47738" : "gray" }}
//           disabled={!isValid}
//           onClick={handleSaveClick}
//         >
//           {t("Deonar_SAVE")}
//         </button>
//       </div>
//     </div>
//   );
// };

// export default SubmitPrintButtonFields;

import React, { useState } from "react";
import { useTranslation } from "react-i18next";

const SubmitPrintButtonFields = ({ submit, print, isValid, handleSave, handlePrint }) => {
  const { t } = useTranslation();
  const [isSaved, setIsSaved] = useState(false);

  const handleSaveClick = () => {
    if (isValid) {
      setIsSaved(true);
      handleSave(); // Call the passed handleSave function
    }
  };

  return (
    <div className="bmc-card-row">
      <div style={{ float: "right", paddingBottom: "1rem", textAlign: "end" }}>
        <button
          type="button"
          className="bmc-card-button"
          style={{
            marginRight: "1rem",
            borderBottom: "3px solid black",
            backgroundColor: isSaved ? "#f47738" : "gray",
          }}
          disabled={!isSaved}
          onClick={handlePrint} // Call the passed handlePrint function
        >
          {t("Deonar_PRINT")}
        </button>
        <button
          type="submit"
          className="bmc-card-button"
          style={{
            marginRight: "1rem",
            borderBottom: "3px solid black",
            backgroundColor: isValid ? "#f47738" : "gray",
          }}
          disabled={!isValid}
          onClick={handleSaveClick}
        >
          {t("Deonar_SAVE")}
        </button>
      </div>
    </div>
  );
};

export default SubmitPrintButtonFields;
