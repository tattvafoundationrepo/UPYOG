import React, { useState } from "react";
import { Card } from "@upyog/digit-ui-react-components";

export const GlobalSearchBar = ({ searchQuery, setSearchQuery }) => {
  return (
    <div>
      <input
        type="text"
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        placeholder="Search across all tables..."
        style={{ border: "1px solid", borderRadius: "8px", padding: "12px" }}
      />
    </div>
  );
};

const TableCard = ({ data, fields, onUUIDClick, searchQuery }) => {
  const matchesSearch = (data) => {
    if (!searchQuery) return true;

    const searchLower = searchQuery.toLowerCase();
    return fields.some((field) => {
      const value = field.display ? field.display(data) : data[field.key];
      return value && String(value).toLowerCase().includes(searchLower);
    });
  };

  if (!matchesSearch(data)) return null;

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
