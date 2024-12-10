import React from "react";
import { CloseSvg } from "@upyog/digit-ui-react-components";

const CustomModal = ({ isOpen, onClose, title, selectedUUID, children, style }) => {
  if (!isOpen) return null;

  return (
    <div className="deonar-modal-overlay" onClick={onClose}>
      <div className="deonar-modal-content" onClick={(e) => e.stopPropagation()} style={style}>
        <div className="deonar-modal-header">
          {selectedUUID ? (
            <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center" }}>
              <h3 style={{ fontWeight: "600", fontSize: "20px" }}>Active Arrival UUID: </h3>
              <span
                style={{
                  fontWeight: "bold",
                  backgroundColor: "rgb(204, 204, 204)",
                  borderRadius: "10px",
                  padding: "8px",
                  fontSize: "22px",
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
          >
            <CloseSvg onClick={onClose} fill="#505A5F" />
          </div>
        </div>
        <div className="deonar-modal-body">{children}</div>
      </div>
    </div>
  );
};

export default CustomModal;
