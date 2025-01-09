import React from "react";

const PrintableReceipt = ({ selectedUUID, filteredGawaltable, generateTokenNumber }) => {
  return (
    <div>
      <style>
        {`
         /* General Container */
.form-container {
  width: 80%;
  margin: 20px auto;
  font-family: "Arial", sans-serif;
  border: 1px solid black;
  padding: 20px;
  background-color: white;
  box-sizing: border-box;
}

/* Top Header */
.top-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.main-header {
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  margin: 10px 0;
}

.original-copy {
  text-align: left;
  font-weight: bold;
  margin: 10px 0;
  display: inline-block;
  border: 1px solid black;
  padding: 2px 10px;
  font-size: 12px;
}

/* Form Section */
.form-section {
  margin-top: 10px;
}

.to-date {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.security-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.allow-text {
  margin: 20px 0;
  line-height: 1.5;
  text-align: justify;
}

/* Table Section */
table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
}

table th,
table td {
  border: 1px solid black;
  text-align: center;
  padding: 5px;
}

table th {
  font-size: 14px;
}

table td {
  height: 30px;
}

/* Footer Section */
.footer {
  margin: 20px 0;
}

.footer p {
  margin: 5px 0;
}

.officer-signature {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
}

/* Right Align Helper */
.right-align {
  text-align: right;
}

          `}
      </style>
      <div className="form-container">
        <div className="top-header">
          <p>Gen-212-BMPP-27858-2017-18-1040 Bks (50x3) Lvs</p>
          <p className="right-align">Sa.-212</p>
        </div>
        <h2 className="main-header">BRIHANMUMBAI MUNICIPAL CORPORATION</h2>
        <p className="original-copy">Original Copy</p>

        <div className="form-section">
          <div className="to-date">
            <p>To,</p>
            <p>Date ..............</p>
          </div>
          <p>No. 010434</p>
          <div className="security-section">
            <p>Security Guard ..............</p>
            <p>Department No. ..............</p>
          </div>
          <p>On duty at this place ....................................................</p>
          <p className="allow-text">
            Please allow Mr/Mrs ................................................ to take the following items from ..................................
            office/work school to ................................................ this place.
          </p>
          <table>
            <thead>
              <tr>
                <th>Requisition Sheet No.</th>
                <th>Details of items</th>
                <th>Quantity</th>
                <th>No.</th>
                <th>Kilograms</th>
              </tr>
            </thead>
            <tbody>
              {Array(8)
                .fill("")
                .map((_, index) => (
                  <tr key={index}>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                  </tr>
                ))}
            </tbody>
          </table>
          <div className="footer">
            <p>Vehicle No. ....................</p>
            <p>Signature of the recipient ....................</p>
          </div>
          <p className="officer-signature">Signature and designation of the officer issuing the order</p>
        </div>
      </div>
    </div>
  );
};

export default PrintableReceipt;
