import React from "react";

const ConfirmationDialog = ({ message, onConfirm, onCancel }) => {
  return (
    <div className="deonar-confirmation-overlay" onClick={onCancel}>
      <div className="deonar-confirmation-content" onClick={(e) => e.stopPropagation()}>
        <div className="deonar-confirmation-header">
          <h3 style={{ fontWeight: "700", fontSize: "20px" }}>Confirmation</h3>
        </div>
        <div className="deonar-confirmation-body">
          <p>{message}</p>
        </div>
        <div className="deonar-confirmation-footer">
          <button onClick={onConfirm} className="deonar-btn-confirm">
            Confirm
          </button>
          <button onClick={onCancel} className="deonar-btn-cancel">
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationDialog;
