import React from 'react';

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
      penalty: "Penalty Charge Receipt"
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
          <div className="receipt-details">
            <div className="detail-row">
              <span className="label">UUID/Reference No.</span>
              <span className="value">{receiptData.uuid || "-"}</span>
            </div>
            <div className="detail-row">
              <span className="label">Payment Date</span>
              <span className="value">{formatDate(receiptData.date) || "-"}</span>
            </div>
            {receiptData.details?.map((detail, index) => (
              <div key={index} className="animal-details">
                <div className="detail-row">
                  <span className="label">Animal Type</span>
                  <span className="value">{detail.animal}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Count</span>
                  <span className="value">{detail.count}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Fee Per Animal</span>
                  <span className="value">₹{detail.fee}</span>
                </div>
              </div>
            ))}
          </div>
        );

      case "parking":
      case "washing":
        return (
          <div className="receipt-details">
            <div className="detail-row">
              <span className="label">Vehicle Number</span>
              <span className="value">{receiptData.vehicleNumber}</span>
            </div>
            <div className="detail-row">
              <span className="label">Vehicle Type</span>
              <span className="value">{receiptData.vehicleType}</span>
            </div>
            <div className="detail-row">
              <span className="label">Entry Date & Time</span>
              <span className="value">
                {formatDate(receiptData.entryDate)} {receiptData.entryTime}
              </span>
            </div>
            <div className="detail-row">
              <span className="label">Exit Date & Time</span>
              <span className="value">
                {formatDate(receiptData.exitDate)} {receiptData.exitTime}
              </span>
            </div>
          </div>
        );

      default:
        return (
          <div className="receipt-details">
            <div className="detail-row">
              <span className="label">Reference Number</span>
              <span className="value">{receiptData.referenceno || "-"}</span>
            </div>
            <div className="detail-row">
              <span className="label">Payment Date</span>
              <span className="value">{formatDate(receiptData.date) || "-"}</span>
            </div>
          </div>
        );
    }
  };

  return (
    <div className="receipt-container font-sans">
      <div className="receipt-header text-center border-b border-gray-300 pb-4 mb-6">
        <h1 className="text-2xl font-bold mb-2">बृहन्मुंबई महानगरपालिका</h1>
        <h2 className="text-xl mb-2">देवनार पशुवधगृह</h2>
        <h3 className="text-lg font-semibold">{getReceiptTitle()}</h3>
        <div className="receipt-number mt-2">
          <span className="font-bold">Receipt No: </span>
          <span>{receiptData.receiptNumber || "000000"}</span>
        </div>
      </div>

      {getReceiptContent()}

      <div className="receipt-footer mt-6 border-t border-gray-300 pt-4">
        <div className="detail-row">
          <span className="label font-bold">Total Amount</span>
          <span className="value font-bold">₹{receiptData.total || receiptData.feevalue || "0"}</span>
        </div>
        <div className="detail-row">
          <span className="label">Payment Method</span>
          <span className="value">{receiptData.method || "-"}</span>
        </div>
        <div className="mt-8 text-right">
          <div className="authorized-signature">
            {t("Authorized Signature")}
          </div>
        </div>
      </div>
    </div>
  );
};

export default EntryFeeReceipt;