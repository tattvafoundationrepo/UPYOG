import React from "react";
import { CloseSvg } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const CustomModal = ({ isOpen, onClose, title, selectedUUID, children, style, width = "auto" }) => {
  const { t } = useTranslation();
  if (!isOpen) return null;

  return (
    <div
      className="deonar-modal-overlay"
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        width: "100vw",
        height: "100vh",
        backgroundColor: "rgba(0, 0, 0, 0.5)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        zIndex: 1000,
        overflow: "hidden",
      }}
      onClick={onClose}
    >
      <div
        className="deonar-modal-content"
        onClick={(e) => e.stopPropagation()}
        style={{
          backgroundColor: "#fff",
          borderRadius: "8px",
          padding: "20px",
          maxWidth: "90%", // Ensures responsiveness
          maxHeight: "90vh", // Ensures the modal doesn't overflow vertically
          overflowY: "auto", // Adds scrolling if content overflows vertically
          width, // Dynamic width passed as a prop
          ...style, // Allows custom styling if passed
        }}
      >
        <div
          className="deonar-modal-header"
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: "20px",
            position: "sticky", // Sticky header
            top: 0,
            backgroundColor: "#fff",
            zIndex: 10,
            padding: "10px 0",
          }}
        >
          {selectedUUID ? (
            <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
              <h3 style={{ fontWeight: "600", fontSize: "20px" }}>{t("Active Arrival UUID")}:</h3>
              <span
                style={{
                  fontWeight: "bold",
                  backgroundColor: "rgb(204, 204, 204)",
                  borderRadius: "10px",
                  padding: "8px",
                  fontSize: "22px",
                  overflow: "hidden",
                  textOverflow: "ellipsis",
                  whiteSpace: "nowrap",
                  maxWidth: "400px",
                }}
              >
                {selectedUUID}
              </span>
            </div>
          ) : (
            <h3 style={{ fontWeight: "700" }}>{title}</h3>
          )}
          <div
            style={{
              backgroundColor: "rgb(204, 204, 204)",
              padding: "10px",
              borderRadius: "8px",
              cursor: "pointer",
            }}
            onClick={onClose}
          >
            <CloseSvg fill="#505A5F" />
          </div>
        </div>
        <div className="deonar-modal-body">{children}</div>
      </div>
    </div>
  );
};

export default CustomModal;
