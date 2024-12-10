// CustomChip.js
import React from "react";

const CustomChip = ({ text, style = {}, onClick }) => {
  return (
    <span
      className="custom-chip"
      style={{
        display: "inline-block",
        padding: "0.2rem 0.5rem",
        backgroundColor: "#f0f0f0",
        borderRadius: "12px",
        fontSize: "1rem",
        margin: "0.2rem",
        cursor: "pointer",
        ...style,
      }}
      onClick={onClick}
    >
      {text}
    </span>
  );
};

export default CustomChip;
