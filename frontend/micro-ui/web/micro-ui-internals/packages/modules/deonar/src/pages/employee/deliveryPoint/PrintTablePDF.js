// import React from "react";

// const PrintablePDFReceipt = (props) => {
//   const currentDate = new Date().toLocaleDateString("en-GB");

//   return (
//     <div style={{ position: "relative", minHeight: "600px" }}>
//       <style>
//         {`
//          /* General Container */
// .form-container {
//   width: 80%;
//   margin: 20px auto;
//   font-family: "Arial", sans-serif;
//   border: 1px solid black;
//   padding: 20px;
//   background-color: white;
//   box-sizing: border-box;

//         }

// /* Top Header */
// .top-header {
//   display: flex;
//   justify-content: space-between;
//   margin-bottom: 10px;
// }

// .main-header {
//   text-align: center;
//   font-size: 18px;
//   font-weight: bold;
//   margin: 10px 0;
// }

// .original-copy {
//   text-align: left;
//   font-weight: bold;
//   margin: 10px 0;
//   display: inline-block;
//   border: 1px solid black;
//   padding: 2px 10px;
//   font-size: 12px;
// }

// /* Form Section */
// .form-section {
//   margin-top: 10px;
// }

// .to-date {
//   display: flex;
//   justify-content: space-between;
//   margin-bottom: 10px;
// }

// .security-section {
//   display: flex;
//   justify-content: space-between;
//   margin-bottom: 10px;
// }

// .allow-text {
//   margin: 20px 0;
//   line-height: 1.5;
//   text-align: justify;
// }

// /* Table Section */
// table {
//   width: 100%;
//   border-collapse: collapse;
//   margin: 20px 0;
// }

// table th,
// table td {
//   border: 1px solid black;
//   text-align: center;
//   padding: 5px;
// }

// table th {
//   font-size: 14px;
// }

// table td {
//   height: 30px;
// }

// /* Footer Section */
// .footer {
//   margin: 20px 0;
// }

// .footer p {
//   margin: 5px 0;
// }

// .officer-signature {
//   text-align: center;
//   margin-top: 20px;
//   font-size: 12px;
// }

// /* Right Align Helper */
// .right-align {
//   text-align: right;
// }

//           `}
//       </style>

//       <div className="form-container">
//         <div
//           style={{
//             position: "absolute",
//             top: 0,
//             left: 0,
//             right: 0,
//             bottom: 0,
//             display: "flex",
//             alignItems: "center",
//             justifyContent: "center",
//             pointerEvents: "none",
//             overflow: "hidden",
//           }}
//         >
//           <img
//             src="https://portal.mcgm.gov.in/com.mcgm.newframework/images/logo.png"
//             alt="Watermark"
//             style={{
//               width: "50%",
//               height: "50%",
//               objectFit: "contain",
//               opacity: 0.1,
//               transform: "rotate(-30deg) scale(1.5)",
//             }}
//           />
//         </div>
//         <h2 className="main-header">BRIHANMUMBAI MUNICIPAL CORPORATION</h2>
//         <p className="original-copy">Original Copy</p>

//         <div className="form-section">
//           <div className="to-date">
//             <p>To,</p>
//             <p>Date: {currentDate}</p>
//           </div>
//           <p>No. </p>
//           <div className="security-section">
//             <p>Security Guard ..............</p>
//             <p>Department No. ..............</p>
//           </div>
//           <p>On duty at this place ....................................................</p>
//           <p className="allow-text">
//             Please allow Mr/Mrs <strong> {props.name} </strong> to take the following items from .................................. office/work school
//             to ................................................ this place.
//           </p>

//           {/* Render Table Dynamically */}
//           <table>
//             <thead>
//               <tr>
//                 <th>Requisition Sheet No.</th>
//                 <th>Details of items</th>
//                 <th>Quantity</th>
//                 <th>No.</th>
//                 <th>Kilograms</th>
//               </tr>
//             </thead>
//             <tbody>
//               {props.tableData &&
//                 props.tableData.map((item, index) => (
//                   <tr key={index}>
//                     <td>{item.ddReference}</td>
//                     <td>{item.animalType}</td>
//                     <td>{item.carcasscount}</td>
//                     <td>{item.kenaecount}</td>
//                     <td>{item.carcasscount + item.kenaecount}</td>
//                   </tr>
//                 ))}
//             </tbody>
//           </table>
//           <div className="footer">
//             <p>Vehicle No. : {props.vehicleNumber}</p>
//             <p>Signature of the recipient ....................</p>
//           </div>
//           <p className="officer-signature">Signature and designation of the officer issuing the order</p>
//         </div>
//       </div>
//     </div>
//   );
// };

// export default PrintablePDFReceipt;

import React from "react";
import { useTranslation } from "react-i18next";

const PrintablePDFReceipt = (props) => {
  const { t } = useTranslation(); // Use the translation hook
  const currentDate = new Date().toLocaleDateString("en-GB");

  return (
    <div style={{ position: "relative", minHeight: "600px" }}>
      <style>
        {`
         /* General Container */
.form-container {
  width: 80%;
  margin: 20px auto;
  font-family: "Arial", sans-serif;
  border: 1px solid black;
  padding: 20px;
  background-color: white;
  box-sizing: border-box;
        }
        

/* Top Header */
.top-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.main-header {
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  margin: 10px 0;
}

.original-copy {
  text-align: left;
  font-weight: bold;
  margin: 10px 0;
  display: inline-block;
  border: 1px solid black;
  padding: 2px 10px;
  font-size: 12px;
}

/* Form Section */
.form-section {
  margin-top: 10px;
}

.to-date {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.security-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.allow-text {
  margin: 20px 0;
  line-height: 1.5;
  text-align: justify;
}

/* Table Section */
table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
}

table th,
table td {
  border: 1px solid black;
  text-align: center;
  padding: 5px;
}

table th {
  font-size: 14px;
}

table td {
  height: 30px;
}

/* Footer Section */
.footer {
  margin: 20px 0;
}

.footer p {
  margin: 5px 0;
}

.officer-signature {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
}

/* Right Align Helper */
.right-align {
  text-align: right;
}

          `}
      </style>

      <div className="form-container">
        <div
          style={{
            position: "absolute",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            pointerEvents: "none",
            overflow: "hidden",
          }}
        >
          <img
            src="https://portal.mcgm.gov.in/com.mcgm.newframework/images/logo.png"
            alt="Watermark"
            style={{
              width: "50%",
              height: "50%",
              objectFit: "contain",
              opacity: 0.1,
              transform: "rotate(-30deg) scale(1.5)",
            }}
          />
        </div>
        <h2 className="main-header">{t("BRIHANMUMBAI MUNICIPAL CORPORATION")}</h2>
        <p className="original-copy">{t("Original Copy")}</p>

        <div className="form-section">
          <div className="to-date">
            <p>{t("To")},</p>
            <p>
              {t("Date")}: {currentDate}
            </p>
          </div>
          <p>
            {t("Reference Number")}: {props.referenceNumber}
          </p>
          <div className="security-section">
            <p>{t("Security Guard")}:</p>
            <p>{t("Department No.")}:</p>
          </div>
          <p>{t("On duty at this place")}:</p>
          <p className="allow-text">
            {t("Please allow Mr/Mrs")} <strong> {props.name} </strong> {t("to take the following items from")}.....{t("office/work school")}.....
            {t("to")}......{t("this place")}
          </p>

          {/* Render Table Dynamically */}
          <table>
            <thead>
              <tr>
                <th>{t("Requisition Sheet No.")}</th>
                <th>{t("Details of items")}</th>
                <th>{t("Quantity")}</th>
                <th>{t("No.")}</th>
                <th>{t("Kilograms")}</th>
              </tr>
            </thead>
            <tbody>
              {props.tableData &&
                props.tableData.map((item, index) => (
                  <tr key={index}>
                    <td>{item.ddReference}</td>
                    <td>{item.animalType}</td>
                    <td>{item.carcasscount}</td>
                    <td>{item.kenaecount}</td>
                    <td>{item.carcasscount + item.kenaecount}</td>
                  </tr>
                ))}
            </tbody>
          </table>
          <div className="footer">
            <p>
              {t("Vehicle No")}: {props.vehicleNumber}
            </p>
            <p>{t("Signature of the recipient")}</p>
          </div>
          <p className="officer-signature">{t("Signature and designation of the officer issuing the order")}</p>
        </div>
      </div>
    </div>
  );
};

export default PrintablePDFReceipt;
