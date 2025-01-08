import React from 'react'

const PrintableReceipt = ({ selectedUUID, filteredGawaltable, generateTokenNumber }) => {
    return (
        <div id="printable-receipt" style={{ display: 'none' }}>
        <style>
          {`
            @media print {
              #printable-receipt {
                display: block !important;
              }
              @page {
                size: A4;
                margin: 20mm;
              }
              body {
                font-family: Arial, sans-serif;
                margin: 20px;
              }
              .container {
                width: 100%;
                text-align: center;
              }
              .heading {
                font-size: 20px;
                font-weight: bold;
              }
              .details {
                margin-top: 20px;
              }
              .details td {
                padding: 5px;
              }
              .signature {
                margin-top: 40px;
                text-align: left;
              }
            }
          `}
        </style>
        <div className="container">
          <div className="heading">
            BRIHANMUMBAI MUNICIPAL CORPORATION
          </div>
          <div className="details">
            <table border="0" width="100%">
              <tbody>
                <tr>
                  <td>To,</td>
                  <td>Date:</td>
                  <td>No.{selectedUUID}</td>
                </tr>
                <tr>
                  <td>Security Guard: ______________</td>
                  <td>Department No.: ______________</td>
                  <td>On duty at: _____________________</td>
                </tr>
                <tr>
                  <td colSpan="3">Please allow Mr/Mrs ___________ to take the following items from ___________ to ___________</td>
                </tr>
              </tbody>
            </table>
            {/* <table border="1" width="100%">
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
                {filteredGawaltable.map((animal, index) => (
                  <tr key={index}>
                    <td>{selectedUUID}</td>
                    <td>{animal.animalType}</td>
                    <td>{animal.count}</td>
                    <td>{generateTokenNumber(animal.animalType, animal.count)}</td>
                    <td>_________</td>
                  </tr>
                ))}
              </tbody>
            </table> */}
            <div className="signature">
              Vehicle No.: __________<br /><br />
              Signature of the recipient: __________<br /><br />
              Signature and designation of the officer issuing the order: __________
            </div>
          </div>
        </div>
      </div>
    );
  };

export default PrintableReceipt