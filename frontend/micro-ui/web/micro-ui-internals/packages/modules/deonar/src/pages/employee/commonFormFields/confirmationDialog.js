import React from "react";
import { useTranslation } from "react-i18next";

const ConfirmationDialog = ({ message, onConfirm, onCancel }) => {
  const { t } = useTranslation();
  return (
    <div className="deonar-confirmation-overlay" onClick={onCancel}>
      <div className="deonar-confirmation-content" onClick={(e) => e.stopPropagation()}>
        <div className="deonar-confirmation-header">
          <h3 style={{ fontWeight: "700", fontSize: "20px" }}>{t("Confirmation")}</h3>
        </div>
        <div className="deonar-confirmation-body">
          <p>{message}</p>
        </div>
        <div className="deonar-confirmation-footer">
          <button onClick={onConfirm} className="deonar-btn-confirm">
            {t("Confirm")}
          </button>
          <button onClick={onCancel} className="deonar-btn-cancel">
            {t("Cancel")}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationDialog;
