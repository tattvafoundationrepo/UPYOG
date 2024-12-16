import React from "react";
import { useTranslation } from "react-i18next";
import CustomTable from "../commonFormFields/customTable";

const FeeConfirmationPage = ({
  id,
  label = "",
  fields = [],
  values = {},
  options = {},
  showTable = false,
  tableData = [],
  isLoading = false,
  Tablecolumns = [],
  feeCollectionResponse,
}) => {
  const { t } = useTranslation();

  const feevalue = feeCollectionResponse?.feevalue;
  const referenceno = feeCollectionResponse?.referenceno;
  const recieptno = feeCollectionResponse?.recieptno;
  const method = feeCollectionResponse?.method;

  const tableColumns = [
    {
      Header: "Animal Type",
      accessor: "animalType",
      disableSortBy: true,
    },
    {
      Header: "Animal Fee",
      accessor: "animalFee",
      disableSortBy: true,
    },
  ];

  return (
    <div
    id={id}
      style={{
        border: "3px dotted #ccc", // Dotted border for a bill-like appearance
        padding: "20px",
        borderRadius: "10px",
        width: "100%",
        maxWidth: "800px",
        margin: "20px auto",
        backgroundColor: "#fff",
        fontFamily: "Arial, sans-serif",
        boxShadow: "0px 0px 10px rgba(0, 0, 0, 0.1)",
        maxHeight:'517px'
      }}
    >
      {/* Bill Header */}
      <div style={{ borderBottom: "2px solid #ddd", paddingBottom: "10px", marginBottom: "20px" }}>
        <h2 style={{ textAlign: "center", fontSize: "24px", margin: "0" }}>{t(label)}</h2>
        <p style={{ textAlign: "center", fontSize: "14px", margin: "10px 0", fontWeight: "bold" }}>Date: {new Date().toLocaleDateString()}</p>
        <p style={{ textAlign: "center", fontSize: "14px", margin: "0", fontWeight: "bold" }}>Receipt No: {recieptno || "-"}</p>
      </div>

      <div style={{ borderBottom: "2px solid #ddd", paddingBottom: "10px", marginBottom: "20px" }}>
        {showTable && (
          <CustomTable
            t={t}
            columns={Tablecolumns}
            tableClassName=" deonar-scrollable-table"
            data={tableData}
            disableSort={false}
            autoSort={false}
            manualPagination={false}
            showSearch={false}
            showTotalRecords={false}
            showPagination={false}
            isLoadingRows={isLoading}
            getCellProps={(cellInfo) => ({
              style: { fontSize: "16px" },
            })}
          />
        )}
      </div>
      {/* Bill Content */}

      {/* <div style={{ paddingBottom: "20px" }}>
        {fields.map((field, index) => (
          <div
            key={index}
            style={{
              display: "flex",
              justifyContent: "space-between",
              marginBottom: "12px",
              borderBottom: "1px solid #f0f0f0",
              paddingBottom: "8px",
            }}
          >
            <label style={{ fontWeight: "bold", fontSize: "16px", width: "50%" }}>{t(field.label)}:</label>
            <div style={{ fontSize: "16px", textAlign: "right", width: "50%" }}>
              {field.type === "dropdown"
                ? options[field.name]?.find((option) => option.value === values[field.name])?.value || "-"
                : values[field.name] || "-"}
            </div>
          </div>
        ))}
      </div> */}
      <div style={{ display: "flex", justifyContent: "space-between", borderTop: "1px solid #ddd", paddingTop: "12px", marginTop: "10px" }}>
        <span style={{ fontWeight: "bold", fontSize: "16px" }}>Amount Received:</span>
        <span style={{ fontSize: "16px" }}>₹ {feevalue || "0"}</span>
      </div>

      <div style={{ display: "flex", justifyContent: "space-between", borderTop: "1px solid #ddd", paddingTop: "12px", marginTop: "10px" }}>
        <span style={{ fontWeight: "bold", fontSize: "16px" }}>Method:</span>
        <span style={{ fontSize: "16px" }}>{method || "-"}</span>
      </div>

      <div style={{ display: "flex", justifyContent: "space-between", borderTop: "1px solid #ddd", paddingTop: "12px", marginTop: "10px" }}>
        <span style={{ fontWeight: "bold", fontSize: "16px" }}>Reference Number:</span>
        <span style={{ fontSize: "16px" }}>{referenceno || "-"}</span>
      </div>

      {/* Balance Field */}
      {/* <div style={{ display: "flex", justifyContent: "space-between", borderTop: "1px solid #ddd", paddingTop: "12px", marginTop: "10px" }}>
        <span style={{ fontWeight: "bold", fontSize: "16px" }}>Balance:</span>
        <span style={{ fontSize: "16px" }}>₹ {1235 || "0"}</span>
      </div> */}

      {/* Bill Summary / Footer */}
      <div style={{ borderTop: "2px solid #ddd", paddingTop: "10px", marginTop: "20px", textAlign: "center" }}>
        <p style={{ fontWeight: "bold", fontSize: "18px" }}>Total: ₹ {feevalue || "0"}</p>
        {/* <p style={{ fontSize: "14px", marginTop: "10px" }}>Thank you for your payment!</p> */}
      </div>
    </div>
  );
};

export default FeeConfirmationPage;
