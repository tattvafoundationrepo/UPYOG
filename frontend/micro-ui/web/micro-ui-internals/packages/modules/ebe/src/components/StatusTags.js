import React from "react";

const StatusTags = ({ statusCounts }) => {
  const tagStyles = [
    { backgroundColor: "#f44336", color: "white" }, // Red
    { backgroundColor: "#2196f3", color: "white" }, // Blue
    { backgroundColor: "#4caf50", color: "white" }, // Green
    { backgroundColor: "#ffeb3b", color: "black" }, // Yellow
  ];

  return (
    <div style={{ display: "flex", gap: "30px", flexWrap: "wrap", alignItems: "stretch" }}>
      {Object.entries(statusCounts).map(([status, count], index) => (
        <div
          key={status}
          style={{
            flex: "1 1 200px", // Flex-basis of 200px with the ability to grow and shrink
            minHeight: "150px", // Minimum height of the cards
            borderRadius: "50px 0 50px 0", // Increased border radius for an oval shape
            ...tagStyles[index % tagStyles.length], // Cycle through styles
            boxShadow: "0 2px 10px rgba(0,0,0,0.1)", // Optional shadow
            overflow: "hidden", // Ensures no content spills out
          }}
        >
          <div
            style={{
              backgroundColor: "rgba(0, 0, 0, 0.2)", // Semi-transparent background for the header
              fontWeight: "bold", // Bold text for the header
              textAlign: "center", // Center text horizontally
              width: "100%", // Ensure header spans the full width of the card
              padding: "20px 20px", // Padding for greater height in the header
            }}
          >
            {status}
          </div>
          <div
            style={{
              flexGrow: 1, // Allows this div to grow and fill space
              display: "flex",
              alignItems: "center", // Center content vertically
              justifyContent: "center", // Center content horizontally
              fontSize: "1.5rem", // Larger font size for the count
              padding: "20px", // Padding around the count
            }}
          >
            {count}
          </div>
        </div>
      ))}
    </div>
  );
};

export default StatusTags;
