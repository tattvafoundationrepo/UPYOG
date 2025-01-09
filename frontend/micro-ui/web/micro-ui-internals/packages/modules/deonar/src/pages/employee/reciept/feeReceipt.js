import React from "react";

const EntryFeeReceipt = ({ receiptData, selectedRadioValue, t }) => {
  const getReceiptTitle = () => {
    const titles = {
      arrival: "Entry Collection Fee Receipt",
      stabling: "Stabling Fee Receipt",
      removal: "Removal Collection Fee Receipt",
      slaughter: "Slaughter Recovery Fee Receipt",
      washing: "Vehicle Washing Fee Receipt",
      parking: "Parking Fee Receipt",
      weighing: "Weighing Charge Receipt",
      penalty: "Penalty Charge Receipt",
    };
    return titles[selectedRadioValue] || "Fee Collection Receipt";
  };

  const formatDate = (date) => {
    if (!date) return "";
    return new Date(date).toLocaleDateString("en-IN");
  };

  const getReceiptContent = () => {
    switch (selectedRadioValue) {
      case "arrival":
      case "stabling":
      case "removal":
        return (
          <div style={{ margin: "10px 0" }}>
            <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
              <span style={{ fontWeight: "500" }}>UUID/Reference No.</span>
              <span>{receiptData.uuid || "-"}</span>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
              <span style={{ fontWeight: "500" }}>Payment Date</span>
              <span>{formatDate(receiptData.date) || "-"}</span>
            </div>
            {receiptData.details?.map((detail, index) => (
              <div key={index} style={{ margin: "10px 0", padding: "10px", border: "1px solid #e5e7eb" }}>
                <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
                  <span style={{ fontWeight: "500" }}>Animal Type</span>
                  <span>{detail.animal}</span>
                </div>
                <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
                  <span style={{ fontWeight: "500" }}>Count</span>
                  <span>{detail.count}</span>
                </div>
                <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
                  <span style={{ fontWeight: "500" }}>Fee Per Animal</span>
                  <span>₹{detail.fee}</span>
                </div>
              </div>
            ))}
          </div>
        );

      case "parking":
      case "washing":
        return (
          <div style={{ margin: "10px 0" }}>
            <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
              <span style={{ fontWeight: "500" }}>Vehicle Number</span>
              <span>{receiptData.vehicleNumber}</span>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
              <span style={{ fontWeight: "500" }}>Vehicle Type</span>
              <span>{receiptData.vehicleType}</span>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
              <span style={{ fontWeight: "500" }}>Entry Date & Time</span>
              <span>
                {formatDate(receiptData.entryDate)} {receiptData.entryTime}
              </span>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
              <span style={{ fontWeight: "500" }}>Exit Date & Time</span>
              <span>
                {formatDate(receiptData.exitDate)} {receiptData.exitTime}
              </span>
            </div>
          </div>
        );

      default:
        return (
          <div style={{ margin: "10px 0" }}>
            <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
              <span style={{ fontWeight: "500" }}>Reference Number</span>
              <span>{receiptData.referenceno || "-"}</span>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
              <span style={{ fontWeight: "500" }}>Payment Date</span>
              <span>{formatDate(receiptData.date) || "-"}</span>
            </div>
          </div>
        );
    }
  };

  return (
    <div style={{ position: "relative", minHeight: "600px" }}>
      {/* Watermark Container */}
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
            // filter: 'grayscale(100%)'
          }}
        />
      </div>

      {/* Receipt Content */}
      <div
        style={{
          fontFamily: "sans-serif",
          position: "relative",
          zIndex: "10",
          padding: "20px",
        }}
      >
        <div
          style={{
            textAlign: "center",
            borderBottom: "1px solid #e5e7eb",
            paddingBottom: "1rem",
            marginBottom: "1.5rem",
          }}
        >
          <h1
            style={{
              fontSize: "1.5rem",
              fontWeight: "bold",
              marginBottom: "0.5rem",
            }}
          >
            बृहन्मुंबई महानगरपालिका
          </h1>
          <h2
            style={{
              fontSize: "1.25rem",
              marginBottom: "0.5rem",
            }}
          >
            देवनार पशुवधगृह
          </h2>
          <h3
            style={{
              fontSize: "1.125rem",
              fontWeight: "600",
            }}
          >
            {getReceiptTitle()}
          </h3>
          <div style={{ marginTop: "0.5rem" }}>
            <span style={{ fontWeight: "bold" }}>Receipt No: </span>
            <span>{receiptData.recieptno || "000000"}</span>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
            <span style={{ fontWeight: "bold" }}>Paid By</span>
            <span style={{ fontWeight: "bold" }}>{receiptData.stakeholdername || "Jane Smith"}</span>
          </div>
        </div>

        {getReceiptContent()}

        <div
          style={{
            marginTop: "1.5rem",
            borderTop: "1px solid #e5e7eb",
            paddingTop: "1rem",
          }}
        >
          <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
            <span style={{ fontWeight: "bold" }}>Total Amount</span>
            <span style={{ fontWeight: "bold" }}>₹{receiptData.total || receiptData.feevalue || "0"}</span>
          </div>

          <div style={{ display: "flex", justifyContent: "space-between", margin: "5px 0" }}>
            <span style={{ fontWeight: "500" }}>Payment Method</span>
            <span>{receiptData.method || "-"}</span>
          </div>
          <div style={{ marginTop: "2rem", textAlign: "right" }}>
            <div>{t("Authorized Signature")}</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EntryFeeReceipt;
