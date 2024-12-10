import React from "react";
import { Card } from "@upyog/digit-ui-react-components";

const TableCard = ({ data, fields, onUUIDClick }) => {
  return (
    <Card
      style={{
        padding: "16px",
        margin: "16px 0",
        boxShadow: "0 2px 10px rgba(0, 0, 0, 0.1)",
        display: "flex",
        flexWrap: "wrap",
        justifyContent: "space-between",
        gap: "20px",
      }}
    >
      {fields.map((field) => {
        const value = field.display ? field.display(data) : data[field.key];

        return (
          <div key={field.key || field.label} style={{ marginBottom: "8px" }}>
            <strong>{field.label}:</strong>{" "}
            {field.isClickable && value ? (
              <span
                onClick={() => onUUIDClick && onUUIDClick(data[field.key])}
                style={{
                  color: "blue",
                  cursor: "pointer",
                  textDecoration: "underline",
                }}
              >
                {value || "N/A"}
              </span>
            ) : (
              <span>{value || "N/A"}</span>
            )}
          </div>
        );
      })}
    </Card>
  );
};

export default TableCard;
