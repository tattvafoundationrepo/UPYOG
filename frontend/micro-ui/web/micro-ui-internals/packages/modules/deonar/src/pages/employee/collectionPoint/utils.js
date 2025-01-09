// // utils.js

import React, { useEffect, useState } from "react";
import GenericAddRow from "../commonFormFields/genericAddRow";
import { useTranslation } from "react-i18next";

// export const deColumns = [
//   {
//     header: "ID",
//     key: "id",
//     render: (value, row, handleUUIDClick, index) => index + 1, // Display the sequence number
//   },
//   {
//    Header: t("DEONAR_ARRIVAL_UUID"),
//     sortable: true,
//     key: "entryUnitId",
//     render: (value, row, handleUUIDClick) => (
//       <span onClick={() => handleUUIDClick(value)} style={{ cursor: "pointer", color: "blue" }}>
//         {value}
//       </span>
//     ),
//   },
//   { Header: t("DEONAR_TRADER_NAME"), key: "traderName" },
//   { Header: t("DEONAR_LICENSE_NUMBER"), key: "licenceNumber" },
//   { header: "DEONAR_VEHICLE_NUMBER", key: "vehicleNumber" },
//   {Header: t("ARRIVAL_DATE_FIELD"), key: "dateOfArrival" },
//   { Header: t("ARRIVAL_TIME_FIELD"), key: "timeOfArrival" },
//   { Header: t("DEONAR_PERMISSION_NUMBER"), key: "importPermission" },
//   { header: "DEONAR_STAKEHOLDER", key: "stakeholderTypeName" },
// ];

export const deColumns = ({ t }) => [
  {
    header: t("ID"),
    key: "id",
    render: (value, row, handleUUIDClick, index) => index + 1, // Display the sequence number
  },
  {
    header: t("DEONAR_ARRIVAL_UUID"),
    sortable: true,
    key: "entryUnitId",
    render: (value, row, handleUUIDClick) => (
      <span onClick={() => handleUUIDClick(value)} style={{ cursor: "pointer", color: "blue" }}>
        {value}
      </span>
    ),
  },
  { header: t("DEONAR_TRADER_NAME"), key: "traderName" },
  { header: t("DEONAR_LICENSE_NUMBER"), key: "licenceNumber" },
  { header: t("DEONAR_VEHICLE_NUMBER"), key: "vehicleNumber" },
  { header: t("ARRIVAL_DATE_FIELD"), key: "dateOfArrival" },
  { header: t("ARRIVAL_TIME_FIELD"), key: "timeOfArrival" },
  { header: t("DEONAR_PERMISSION_NUMBER"), key: "importPermission" },
  { header: t("DEONAR_STAKEHOLDER"), key: "stakeholderTypeName" },
];

export const columns = (handleUUIDClick, t) => [
  {
    Header: t("ID"),
    accessor: "id",
    Cell: ({ row }) => row.index + 1,
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: false,
  },
  {
    Header: t("DEONAR_ARRIVAL_UUID"),
    accessor: "entryUnitId",
    sortable: true,
    Cell: ({ row }) => (
      <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
        {row.original.entryUnitId}
      </span>
    ),
    isVisible: true,
  },
  {
    Header: t("DEONAR_TRADER_NAME"),
    accessor: "traderName",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("DEONAR_LICENSE_NUMBER"),
    accessor: "licenceNumber",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("DEONAR_PERMISSION_NUMBER"),
    accessor: "importPermission",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("ARRIVAL_DATE_FIELD"),
    accessor: "date",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("ARRIVAL_TIME_FIELD"),
    accessor: "time",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
];

// export const createDynamicColumns = (handleUUIDClick, tableType, t) => {
//   const baseColumns = [
//     {
//       Header: t("ID"),
//       accessor: "id",
//       Cell: ({ row }) => row.index + 1,
//       isVisible: false,
//     },
//     {
//       Header: t("DEONAR_ARRIVAL_UUID"),
//       accessor: "entryUnitId",
//       Cell: ({ row }) => (
//         <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
//           {row.original.entryUnitId}
//         </span>
//       ),
//     },
//     {
//       Header: t("DEONAR_TRADER_NAME"),
//       accessor: "traderName",
//     },
//     {
//       Header: t("DEONAR_PERMISSION_NUMBER"),
//       accessor: "importPermission",
//     },
//     {
//       Header: t("DEONAR_LICENSE_NUMBER"),
//       accessor: "licenceNumber",
//     },
//     {
//       Header: t("ARRIVAL_DATE_FIELD"),
//       accessor: "date",
//     },
//     {
//       Header: "ARRIVAL_TIME_FIELD",
//       accessor: "time",
//     },
//   ];

//   if (tableType === "stabling") {
//     return [
//       {
//         Header: "ID",
//         accessor: "stakeholderId",
//         Cell: ({ row }) => row.index + 1,
//         isVisible: false,
//       },
//       {
//         Header: "DEONAR_ARRIVAL_UUID",
//         accessor: "entryUnitId",
//         Cell: ({ row }) => (
//           <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
//             {row.original.entryUnitId}
//           </span>
//         ),
//       },
//       {
//         Header: "DEONAR_TRADER_NAME",
//         accessor: "traderName",
//       },
//       {
//         Header: t("DEONAR_LICENSE_NUMBER"),
//         accessor: "licenceNumber",
//       },
//       {
//         Header: "DEONAR_MOBILE_NUMBER",
//         accessor: "mobileNumber",
//       },
//       {
//         Header: "DEONAR_STAKEHOLDER_TYPE",
//         accessor: "stakeholderTypeName",
//       },
//       {
//         Header: "DEONAR_REGISTRATION_NUMBER",
//         accessor: "registrationNumber",
//       },
//     ];
//   }

//   if (tableType === "arrival") {
//     return [
//       {
//         Header: "ID",
//         accessor: "stakeholderId",
//         Cell: ({ row }) => row.index + 1,
//         isVisible: false,
//       },
//       {
//         Header: "DEONAR_ARRIVAL_UUID",
//         accessor: "entryUnitId",
//         Cell: ({ row }) => (
//           <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
//             {row.original.entryUnitId}
//           </span>
//         ),
//       },
//       {
//         Header: "DEONAR_TRADER_NAME",
//         accessor: "traderName",
//       },
//       {
//         Header: t("DEONAR_LICENSE_NUMBER"),
//         accessor: "licenceNumber",
//       },
//       {
//         Header: "DEONAR_MOBILE_NUMBER",
//         accessor: "mobileNumber",
//       },
//       {
//         Header: "DEONAR_STAKEHOLDER_TYPE",
//         accessor: "stakeholderTypeName",
//       },
//       {
//         Header: "DEONAR_REGISTRATION_NUMBER",
//         accessor: "registrationNumber",
//       },
//     ];
//   }

//   if (tableType === "parking") {
//     return [
//       {
//         Header: "ID",
//         accessor: "id",
//         Cell: ({ row }) => row.index + 1,
//         getHeaderProps: (column) => ({
//           style: {
//             display: "flex",
//             alignItems: "center",
//             justifyContent: "center",
//           },
//         }),
//         isVisible: false,
//       },
//       {
//         Header: "Deonar_Vehicle_Number",
//         accessor: "vehicleNumber",
//         Cell: ({ row }) => (
//           <span
//             onClick={() => handleUUIDClick(row.original.vehicleNumber, row.original.vehicleId, tableType)}
//             style={{ cursor: "pointer", color: "blue" }}
//           >
//             {row.original.vehicleNumber}
//           </span>
//         ),
//       },
//       {
//         Header: "Deonar_Vehicle_Type",
//         accessor: "vehicleType",
//       },

//       {
//         Header: "Deonar_Parking_Time",
//         accessor: "parkingTime",
//       },
//       {
//         Header: "Deonar_Parking_Date",
//         accessor: "parkingDate",
//       },
//     ];
//   }

//   if (tableType === "washing") {
//     return [
//       {
//         Header: "ID",
//         accessor: "id",
//         Cell: ({ row }) => row.index + 1,
//         getHeaderProps: (column) => ({
//           style: {
//             display: "flex",
//             alignItems: "center",
//             justifyContent: "center",
//           },
//         }),
//         isVisible: false,
//       },
//       {
//         Header: "Deonar_Vehicle_Number",
//         accessor: "vehicleNumber",
//         Cell: ({ row }) => (
//           <span
//             onClick={() => handleUUIDClick(row.original.vehicleNumber, row.original.vehicleId, tableType)}
//             style={{ cursor: "pointer", color: "blue" }}
//           >
//             {row.original.vehicleNumber}
//           </span>
//         ),
//       },
//       {
//         Header: "Deonar_Vehicle_Type",
//         accessor: "vehicleType",
//       },

//       {
//         Header: "Deonar_Washing_Time",
//         accessor: "washingTime",
//       },
//       {
//         Header: "Deonar_Washing_Date",
//         accessor: "washingDate",
//       },
//     ];
//   }

//   if (tableType === "slaughter") {
//     return [
//       {
//         Header: "ID",
//         accessor: "id",
//         Cell: ({ row }) => row.index + 1,
//         getHeaderProps: (column) => ({
//           style: {
//             display: "flex",
//             alignItems: "center",
//             justifyContent: "center",
//           },
//         }),
//         isVisible: false,
//       },
//       {
//         Header: "Deonar_DD_Reference",
//         accessor: "ddReference",
//         Cell: ({ row }) => (
//           <span onClick={() => handleUUIDClick(row.original.ddReference)} style={{ cursor: "pointer", color: "blue" }}>
//             {row.original.ddReference}
//           </span>
//         ),
//       },

//       {
//         Header: "DEONAR_SHOPKEEPER_NAME",
//         accessor: "shopkeeperName",
//       },
//       {
//         Header: t("DEONAR_LICENSE_NUMBER"),
//         accessor: "licenceNumber",
//       },
//       {
//         Header: "DEONAR_ARRIVAL_UUID",
//         accessor: "arrivalId",
//       },
//     ];
//   }

//   if (tableType === "trading") {
//     return [
//       {
//         Header: "ID",
//         accessor: "id",
//         Cell: ({ row }) => row.index + 1,
//         isVisible: false,
//       },
//       {
//         Header: "Deonar_DD_Reference",
//         accessor: "ddreference",
//         Cell: ({ row }) => (
//           <span onClick={() => handleUUIDClick(row.original.ddreference)} style={{ cursor: "pointer", color: "blue" }}>
//             {row.original.ddreference}
//           </span>
//         ),
//       },
//       {
//         Header: "DEONAR_SHOPKEEPER_NAME",
//         accessor: "traderName",
//       },
//       {
//         Header: t("DEONAR_LICENSE_NUMBER"),
//         accessor: "licenceNumber",
//       },
//       {
//         Header: "DEONAR_PERMISSION_NUMBER",
//         accessor: "importPermission",
//       },
//       {
//         Header: "DEONAR_ARRIVAL_UUID",
//         accessor: "entryUnitId",
//       },
//       {
//         Header: t("ARRIVAL_DATE_FIELD"),
//         accessor: "date",
//       },
//       {
//         Header: "ARRIVAL_TIME_FIELD",
//         accessor: "time",
//       },
//     ];
//   }

//   if (tableType === "removal") {
//     return [
//       {
//         Header: "ID",
//         accessor: "stakeholderId",
//         Cell: ({ row }) => row.index + 1,
//         isVisible: false,
//       },
//       {
//         Header: "DEONAR_ARRIVAL_UUID",
//         accessor: "entryUnitId",
//         Cell: ({ row }) => (
//           <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
//             {row.original.entryUnitId}
//           </span>
//         ),
//       },
//       {
//         Header: "DEONAR_TRADER_NAME",
//         accessor: "traderName",
//       },
//       {
//         Header: t("DEONAR_LICENSE_NUMBER"),
//         accessor: "licenceNumber",
//       },
//       {
//         Header: "DEONAR_MOBILE_NUMBER",
//         accessor: "mobileNumber",
//       },
//       {
//         Header: "DEONAR_STAKEHOLDER_TYPE",
//         accessor: "stakeholderTypeName",
//       },
//       // {
//       //   Header: "DEONAR_REGISTRATION_NUMBER",
//       //   accessor: "registrationNumber",
//       // },
//     ];
//   }

//   if (tableType === "penalty") {
//     return [
//       {
//         Header: "ID",
//         accessor: "id",
//         Cell: ({ row }) => row.index + 1,
//         isVisible: false,
//       },
//       {
//         Header: "Deonar_Penalty_Reference",
//         accessor: "penaltyReference",
//         Cell: ({ row }) => (
//           <span onClick={() => handleUUIDClick(row.original.penaltyReference)} style={{ cursor: "pointer", color: "blue" }}>
//             {row.original.penaltyReference}
//           </span>
//         ),
//       },
//       {
//         Header: "DEONAR_StakeHolder_NAME",
//         accessor: "stakeholderName",
//       },
//       {
//         Header: t("DEONAR_LICENSE_NUMBER"),
//         accessor: "licenceNumber",
//       },
//     ];
//   }
//   if (tableType === "weighing") {
//     return [
//       {
//         Header: "ID",
//         accessor: "id",
//         Cell: ({ row }) => row.index + 1,
//         getHeaderProps: (column) => ({
//           style: {
//             display: "flex",
//             alignItems: "center",
//             justifyContent: "center",
//           },
//         }),
//         isVisible: false,
//       },
//       {
//         Header: "Deonar_DD_Reference",
//         accessor: "ddReference",
//         Cell: ({ row }) => (
//           <span onClick={() => handleUUIDClick(row.original.ddReference)} style={{ cursor: "pointer", color: "blue" }}>
//             {row.original.ddReference}
//           </span>
//         ),
//       },

//       {
//         Header: "DEONAR_SHOPKEEPER_NAME",
//         accessor: "shopkeeperName",
//       },
//       {
//         Header: t("DEONAR_LICENSE_NUMBER"),
//         accessor: "licenceNumber",
//       },
//       {
//         Header: "DEONAR_ARRIVAL_UUID",
//         accessor: "arrivalId",
//       },
//     ];
//   }

//   return baseColumns;
// };

export const createDynamicColumns = (handleUUIDClick, tableType, t = (text) => text) => {
  if (typeof t !== "function") {
    console.warn("Translation function not provided, using default");
    t = (text) => text;
  }

  try {
    const baseColumns = [
      {
        Header: t("ID"),
        accessor: "id",
        Cell: ({ row }) => row.index + 1,
        isVisible: false,
      },
      {
        Header: t("DEONAR_ARRIVAL_UUID"),
        accessor: "entryUnitId",
        Cell: ({ row }) => (
          <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
            {row.original.entryUnitId}
          </span>
        ),
      },
      {
        Header: t("DEONAR_TRADER_NAME"),
        accessor: "traderName",
      },
      {
        Header: t("DEONAR_PERMISSION_NUMBER"),
        accessor: "importPermission",
      },
      {
        Header: t("DEONAR_LICENSE_NUMBER"),
        accessor: "licenceNumber",
      },
      {
        Header: t("ARRIVAL_DATE_FIELD"),
        accessor: "date",
      },
      {
        Header: t("ARRIVAL_TIME_FIELD"),
        accessor: "time",
      },
    ];

    // Rest of your switch cases for different table types
    switch (tableType) {
      case "stabling":
        return [
          {
            Header: t("ID"),
            accessor: "stakeholderId",
            Cell: ({ row }) => row.index + 1,
            isVisible: false,
          },
          {
            Header: t("DEONAR_ARRIVAL_UUID"),
            accessor: "entryUnitId",
            Cell: ({ row }) => (
              <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
                {row.original.entryUnitId}
              </span>
            ),
          },
          {
            Header: t("DEONAR_TRADER_NAME"),
            accessor: "traderName",
          },
          {
            Header: t("DEONAR_LICENSE_NUMBER"),
            accessor: "licenceNumber",
          },
          {
            Header: t("DEONAR_MOBILE_NUMBER"),
            accessor: "mobileNumber",
          },
          {
            Header: t("DEONAR_STAKEHOLDER_TYPE"),
            accessor: "stakeholderTypeName",
          },
          {
            Header: t("DEONAR_REGISTRATION_NUMBER"),
            accessor: "registrationNumber",
          },
        ];
      case "arrival":
        return [
          {
            Header: t("ID"),
            accessor: "stakeholderId",
            Cell: ({ row }) => row.index + 1,
            isVisible: false,
          },
          {
            Header: t("DEONAR_ARRIVAL_UUID"),
            accessor: "entryUnitId",
            Cell: ({ row }) => (
              <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
                {row.original.entryUnitId}
              </span>
            ),
          },
          {
            Header: t("DEONAR_TRADER_NAME"),
            accessor: "traderName",
          },
          {
            Header: t("DEONAR_LICENSE_NUMBER"),
            accessor: "licenceNumber",
          },
          {
            Header: t("DEONAR_MOBILE_NUMBER"),
            accessor: "mobileNumber",
          },
          {
            Header: t("DEONAR_STAKEHOLDER_TYPE"),
            accessor: "stakeholderTypeName",
          },
          {
            Header: t("DEONAR_REGISTRATION_NUMBER"),
            accessor: "registrationNumber",
          },
        ];
      case "parking":
        return [
          {
            Header: t("ID"),
            accessor: "id",
            Cell: ({ row }) => row.index + 1,
            getHeaderProps: (column) => ({
              style: {
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              },
            }),
            isVisible: false,
          },
          {
            Header: t("Deonar_Vehicle_Number"),
            accessor: "vehicleNumber",
            Cell: ({ row }) => (
              <span
                onClick={() => handleUUIDClick(row.original.vehicleNumber, row.original.vehicleId, tableType)}
                style={{ cursor: "pointer", color: "blue" }}
              >
                {row.original.vehicleNumber}
              </span>
            ),
          },
          {
            Header: t("Deonar_Vehicle_Type"),
            accessor: "vehicleType",
          },

          {
            Header: t("Deonar_Parking_Time"),
            accessor: "parkingTime",
          },
          {
            Header: t("Deonar_Parking_Date"),
            accessor: "parkingDate",
          },
        ];

      case "washing": {
        return [
          {
            Header: t("ID"),
            accessor: "id",
            Cell: ({ row }) => row.index + 1,
            getHeaderProps: (column) => ({
              style: {
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              },
            }),
            isVisible: false,
          },
          {
            Header: t("Deonar_Vehicle_Number"),
            accessor: "vehicleNumber",
            Cell: ({ row }) => (
              <span
                onClick={() => handleUUIDClick(row.original.vehicleNumber, row.original.vehicleId, tableType)}
                style={{ cursor: "pointer", color: "blue" }}
              >
                {row.original.vehicleNumber}
              </span>
            ),
          },
          {
            Header: t("Deonar_Vehicle_Type"),
            accessor: "vehicleType",
          },

          {
            Header: t("Deonar_Washing_Time"),
            accessor: "washingTime",
          },
          {
            Header: t("Deonar_Washing_Date"),
            accessor: "washingDate",
          },
        ];
      }

      case "slaughter": {
        return [
          {
            Header: t("ID"),
            accessor: "stakeholderId",
            Cell: ({ row }) => row.index + 1,
            isVisible: false,
          },
          {
            Header: t("Deonar_DD_Reference"),
            accessor: "ddreference",
            Cell: ({ row }) => (
              <span onClick={() => handleUUIDClick(row.original.ddreference)} style={{ cursor: "pointer", color: "blue" }}>
                {row.original.ddreference}
              </span>
            ),
          },
          {
            Header: t("DEONAR_TRADER_NAME"),
            accessor: "traderName",
          },
          {
            Header: t("DEONAR_MOBILE_NUMBER"),
            accessor: "mobileNumber",
          },
          {
            Header: t("DEONAR_ARRIVAL_UUID"),
            accessor: "entryUnitId",
          },
          {
            Header: t("DEONAR_STAKEHOLDER_TYPE"),
            accessor: "stakeholderTypeName",
          },
        ];
      }

      case "trading": {
        return [
          {
            Header: t("ID"),
            accessor: "id",
            Cell: ({ row }) => row.index + 1,
            isVisible: false,
          },
          {
            Header: t("Deonar_DD_Reference"),
            accessor: "ddreference",
            Cell: ({ row }) => (
              <span onClick={() => handleUUIDClick(row.original.ddreference)} style={{ cursor: "pointer", color: "blue" }}>
                {row.original.ddreference}
              </span>
            ),
          },
          {
            Header: t("DEONAR_SHOPKEEPER_NAME"),
            accessor: "traderName",
          },
          {
            Header: t("DEONAR_LICENSE_NUMBER"),
            accessor: "licenceNumber",
          },
          {
            Header: t("DEONAR_PERMISSION_NUMBER"),
            accessor: "importPermission",
          },
          {
            Header: t("DEONAR_ARRIVAL_UUID"),
            accessor: "entryUnitId",
          },
          {
            Header: t("ARRIVAL_DATE_FIELD"),
            accessor: "date",
          },
          {
            Header: t("ARRIVAL_TIME_FIELD"),
            accessor: "time",
          },
        ];
      }

      case "removal": {
        return [
          {
            Header: t("ID"),
            accessor: "stakeholderId",
            Cell: ({ row }) => row.index + 1,
            isVisible: false,
          },
          {
            Header: t("DEONAR_ARRIVAL_UUID"),
            accessor: "entryUnitId",
            Cell: ({ row }) => (
              <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
                {row.original.entryUnitId}
              </span>
            ),
          },
          {
            Header: t("DEONAR_TRADER_NAME"),
            accessor: "traderName",
          },
          {
            Header: t("DEONAR_LICENSE_NUMBER"),
            accessor: "licenceNumber",
          },
          {
            Header: t("DEONAR_MOBILE_NUMBER"),
            accessor: "mobileNumber",
          },
          {
            Header: t("DEONAR_STAKEHOLDER_TYPE"),
            accessor: "stakeholderTypeName",
          },
          // {
          //   Header: "DEONAR_REGISTRATION_NUMBER",
          //   accessor: "registrationNumber",
          // },
        ];
      }

      case "penalty": {
        return [
          {
            Header: t("ID"),
            accessor: "id",
            Cell: ({ row }) => row.index + 1,
            isVisible: false,
          },
          {
            Header: t("Deonar_Penalty_Reference"),
            accessor: "penaltyReference",
            Cell: ({ row }) => (
              <span onClick={() => handleUUIDClick(row.original.penaltyReference)} style={{ cursor: "pointer", color: "blue" }}>
                {row.original.penaltyReference}
              </span>
            ),
          },
          {
            Header: t("DEONAR_StakeHolder_NAME"),
            accessor: "stakeholderName",
          },
          {
            Header: t("DEONAR_LICENSE_NUMBER"),
            accessor: "licenceNumber",
          },
        ];
      }
      case "weighing": {
        return [
          {
            Header: t("ID"),
            accessor: "id",
            Cell: ({ row }) => row.index + 1,
            getHeaderProps: (column) => ({
              style: {
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              },
            }),
            isVisible: false,
          },
          {
            Header: t("Deonar_DD_Reference"),
            accessor: "ddReference",
            Cell: ({ row }) => (
              <span onClick={() => handleUUIDClick(row.original.ddReference)} style={{ cursor: "pointer", color: "blue" }}>
                {row.original.ddReference}
              </span>
            ),
          },

          {
            Header: t("DEONAR_SHOPKEEPER_NAME"),
            accessor: "shopkeeperName",
          },
          {
            Header: t("DEONAR_LICENSE_NUMBER"),
            accessor: "licenceNumber",
          },
          {
            Header: t("DEONAR_ARRIVAL_UUID"),
            accessor: "arrivalId",
          },
        ];
      }
      default:
        return baseColumns;
    }
  } catch (error) {
    console.error("Error in createDynamicColumns:", error);
    return []; // Return empty array in case of error
  }
};

export const helkariColumns = (handleUUIDClick, t) => [
  {
    Header: t("ID"),
    accessor: "id",
    Cell: ({ row }) => row.index + 1,
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: false,
  },
  {
    Header: t("Deonar_DD_Reference"),
    accessor: "ddreference",
    sortable: true,
    Cell: ({ row }) => (
      <span onClick={() => handleUUIDClick(row.original.ddreference)} style={{ cursor: "pointer", color: "blue" }}>
        {row.original.ddreference}
      </span>
    ),
    isVisible: true,
  },
  {
    Header: t("DEONAR_SHOPKEEPER_NAME"),
    accessor: "traderName",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },

  {
    Header: t("DEONAR_LICENSE_NUMBER"),
    accessor: "licenceNumber",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("DEONAR_ARRIVAL_UUID"),
    accessor: "entryUnitId",
  },
  {
    Header: t("ARRIVAL_DATE_FIELD"),
    accessor: "date",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("ARRIVAL_TIME_FIELD"),
    accessor: "time",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
];

export const DawanWalaColumns = (handleUUIDClick, t) => [
  {
    Header: t("ID"),
    accessor: "id",
    Cell: ({ row }) => row.index + 1,
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: false,
  },
  {
    Header: t("Deonar_DD_Reference"),
    accessor: "ddreference",
    sortable: true,
    Cell: ({ row }) => (
      <span onClick={() => handleUUIDClick(row.original.ddreference)} style={{ cursor: "pointer", color: "blue" }}>
        {row.original.ddreference}
      </span>
    ),
    isVisible: true,
  },
  {
    Header: t("DEONAR_SHOPKEEPER_NAME"),
    accessor: "traderName",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("DEONAR_LICENSE_NUMBER"),
    accessor: "licenceNumber",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("DEONAR_ARRIVAL_UUID"),
    accessor: "entryUnitId",
  },
  {
    Header: t("ARRIVAL_DATE_FIELD"),
    accessor: "date",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("ARRIVAL_TIME_FIELD"),
    accessor: "time",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
];

export const CitizenColumns = (handleUUIDClick, t) => [
  {
    Header: t("ID"),
    accessor: "id",
    Cell: ({ row }) => row.index + 1,
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: false,
  },
  {
    Header: t("DEONAR_ARRIVAL_UUID"),
    accessor: "entryUnitId",
    sortable: true,
    Cell: ({ row }) => (
      <span onClick={() => handleUUIDClick(row.original.entryUnitId)} style={{ cursor: "pointer", color: "blue" }}>
        {row.original.entryUnitId}
      </span>
    ),
    isVisible: true,
  },
  {
    Header: t("DEONAR_CITIZEN_NAME"),
    accessor: "traderName",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("DEONAR_PERMISSION_NUMBER"),
    accessor: "importPermission",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("ARRIVAL_DATE_FIELD"),
    accessor: "date",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
  {
    Header: t("ARRIVAL_TIME_FIELD"),
    accessor: "time",
    getHeaderProps: (column) => ({
      style: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // or any other styles you want
      },
    }),
    isVisible: true,
  },
];

export function generateDynamicData(length, startId = 1) {
  const traderNames = [
    "John Doe",
    "Jane Smith",
    "Mohammed Ali",
    "Sophia Wilson",
    "David Lee",
    "Emily Johnson",
    "Michael Brown",
    "Sarah Davis",
    "James Clark",
    "Mary Evans",
  ];
  const stakeholderTypes = ["Trader", "Owner", "Broker", "Agent"];

  const randomDate = () => {
    const date = new Date();
    date.setDate(date.getDate() - Math.floor(Math.random() * 30));
    return date.toISOString().split("T")[0]; // Format to YYYY-MM-DD
  };

  const randomTime = () => {
    const hours = Math.floor(Math.random() * 12) + 1;
    const minutes = Math.floor(Math.random() * 60);
    const ampm = Math.random() > 0.5 ? "AM" : "PM";
    return `${hours}:${minutes < 10 ? "0" : ""}${minutes} ${ampm}`;
  };

  return Array.from({ length }, (_, i) => ({
    id: startId + i,
    entryUnitId: `UUID-${Math.floor(1000 + Math.random() * 9000)}`,
    traderName: traderNames[Math.floor(Math.random() * traderNames.length)],
    licenceNumber: `LIC-${Math.floor(1000 + Math.random() * 9000)}`,
    vehicleNumber: `MH${Math.floor(10 + Math.random() * 90)}${["AB", "CD", "EF", "GH"][Math.floor(Math.random() * 4)]}${Math.floor(
      1000 + Math.random() * 9000
    )}`,
    dateOfArrival: randomDate(),
    timeOfArrival: randomTime(),
    importPermission: `IMP-${Math.floor(1000 + Math.random() * 9000)}`,
    stakeholderTypeName: stakeholderTypes[Math.floor(Math.random() * stakeholderTypes.length)],
  }));
}

export const useDebounce = (value, delay) => {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);
    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
};

export const visibleColumns = [
  { Header: "Animal Type", accessor: "animalType" },
  { Header: "Animal Token", accessor: "count" },
  {
    accessor: "animalDetails",
    Header: "Gawal Details",
    Cell: ({ row }) => <GenericAddRow />,
  },
];

export const generateTokenNumber = (animalName, index) => {
  const prefix = animalName?.substring(0, 2).toUpperCase();
  const tokenNumber = `${prefix}${String(index).padStart(3, "0")}`;
  return tokenNumber;
};

const paymentMethodOptions = [
  { label: "Cash", value: "Cash" },
  { label: "Card", value: "Card" },
  { label: "UPI", value: "UPI" },
];

// const slaughterTypeOptions = [
//   { label: "Normal", value: "Normal" },
//   { label: "Emergency", value: "Emergency" },
// ];

export const feeConfigs = {
  arrival: {
    fields: [
      { type: "dropdown", label: "Payment Mode", name: "paymentMethod", required: true },
      { type: "input", label: "Reference Number", name: "transactionId", required: true },
    ],
    options: {
      paymentMethod: paymentMethodOptions,
    },
  },
  stabling: {
    fields: [
      { type: "dropdown", label: "Payment Mode", name: "paymentMethod", required: true },
      { type: "input", label: "Reference Number", name: "transactionId", required: true },
    ],
    options: {
      paymentMethod: paymentMethodOptions,
    },
  },
  trading: {
    fields: [
      { type: "dropdown", label: "Payment Mode", name: "paymentMethod", required: true },
      { type: "input", label: "Reference Number", name: "transactionId", required: true },
    ],
    options: {
      paymentMethod: paymentMethodOptions,
    },
  },
  removal: {
    fields: [
      { type: "dropdown", label: "Payment Mode", name: "paymentMethod", required: true },
      { type: "input", label: "Reference Number", name: "transactionId", required: true },
    ],
    options: {
      paymentMethod: paymentMethodOptions,
    },
  },
  slaughter: {
    fields: [
      // { type: "dropdown", label: "Slaughter Unit Type", name: "slaughterType", required: true },
      // { type: "dropdown", label: "Slaughter Unit", name: "slaughterUnit", required: true },
      // { type: "dropdown", label: "Unit Shift", name: "unitShift", required: true },
      // { type: "input", label: "Slaughter Unit Charge", name: "slaughterUnitCharge", required: false },
      { type: "dropdown", label: "Payment Mode", name: "paymentMethod", required: true },
      { type: "input", label: "Reference Number", name: "transactionId", required: true },
    ],
    options: {
      paymentMethod: paymentMethodOptions,
      // slaughterType: slaughterTypeOptions,
      // slaughterUnit: [],
      // unitShift: [],
    },
  },
  washing: {
    fields: [
      { type: "dropdown", label: "Payment Mode", name: "paymentMethod", required: true },
      { type: "input", label: "Reference Number", name: "transactionId", required: true },
    ],
    options: {
      paymentMethod: paymentMethodOptions,
    },
  },
  parking: {
    fields: [
      // { type: "input", label: "Deonar Parking Amount", name: "parkingAmount", required: false },
      { type: "dropdown", label: "Payment Mode", name: "paymentMethod", required: true },
      { type: "input", label: "Reference Number", name: "transactionId", required: true },
    ],
    options: {
      paymentMethod: paymentMethodOptions,
    },
  },
  weighing: {
    fields: [
      { type: "dropdown", label: "Payment Mode", name: "paymentMethod", required: true },
      { type: "input", label: "Reference Number", name: "transactionId", required: true },
    ],
    options: {
      paymentMethod: paymentMethodOptions,
    },
  },
  penalty: {
    fields: [
      { type: "dropdown", label: "Payment Mode", name: "paymentMethod", required: true },
      { type: "input", label: "Reference Number", name: "transactionId", required: true },
    ],
    options: {
      paymentMethod: paymentMethodOptions,
    },
  },
};

// export const collectionDynamicColumns ={ 
  
//   arrival: [
//     {
//       Header: "Animal Type",
//       accessor: "animalType",
//       disableSortBy: true,
//     },
//     {
//       Header: "Animal Count",
//       accessor: "animalCount",
//       disableSortBy: true,
//     },
//     {
//       Header: "Animal Fee",
//       accessor: "animalFee",
//       disableSortBy: true,
//     },
//     {
//       Header: "Animal Total Fee",
//       accessor: "totalFee",
//       disableSortBy: true,
//     },
//   ],
//   stabling: [
//     {
//       Header: "Animal Type",
//       accessor: "animalType",
//       disableSortBy: true,
//     },
//     {
//       Header: "Animal Count",
//       accessor: "animalCount",
//       disableSortBy: true,
//     },
//     {
//       Header: "Total Stabling Fee",
//       accessor: "totalFee",
//       disableSortBy: true,
//     },
//     {
//       Header: "Details",
//       disableSortBy: true,
//     },
//   ],
//   washing: [
//     {
//       Header: "Vehicle Number",
//       accessor: "vehiclenumber",
//       disableSortBy: true,
//     },
//     {
//       Header: "Vehicle Type",
//       accessor: "vehicletype",
//       disableSortBy: true,
//     },
//     {
//       Header: "Total Washing Fee",
//       accessor: "total",
//       disableSortBy: true,
//     },
//   ],
//   parking: [
//     {
//       Header: "Vehicle Number",
//       accessor: "vehiclenumber",
//       disableSortBy: true,
//     },
//     {
//       Header: "Vehicle Type",
//       accessor: "vehicletype",
//       disableSortBy: true,
//     },
//     {
//       Header: "Parking Date",
//       accessor: "parkingdate",
//       disableSortBy: true,
//     },
//     {
//       Header: "Parking Time",
//       accessor: "parkingtime",
//       disableSortBy: true,
//     },
//     {
//       Header: "Departure Date",
//       accessor: "departuredate",
//       disableSortBy: true,
//     },
//     {
//       Header: "Departure Time",
//       accessor: "departuretime",
//       disableSortBy: true,
//     },
//     {
//       Header: "Total Hours",
//       accessor: "totalhours",
//       disableSortBy: true,
//     },
//   ],
//   slaughter: [
//     {
//       Header: "Animal",
//       accessor: "animalType",
//       disableSortBy: true,
//     },
//     {
//       Header: "Animal Count",
//       accessor: "animalCount",
//       disableSortBy: true,
//     },
//     {
//       Header: "Slaughter Fee",
//       accessor: "animalFee",
//       disableSortBy: true,
//     },
//     {
//       Header: "Total Slaughter Fee",
//       accessor: "totalFee",
//       disableSortBy: true,
//     },
//     {
//       Header: "Details",
//       disableSortBy: true,
//     },
//   ],
//   removal: [
//     {
//       Header: "Animal Type",
//       accessor: "animalType",
//       disableSortBy: true,
//     },
//     {
//       Header: "Animal Count",
//       accessor: "animalCount",
//       disableSortBy: true,
//     },
//     {
//       Header: "Removal Fee",
//       accessor: "totalFee",
//       disableSortBy: true,
//     },
//     {
//       Header: "Details",
//       disableSortBy: true,
//     },
//   ],
//   trading: [
//     {
//       Header: "Animal Type",
//       accessor: "animalType",
//       disableSortBy: true,
//     },
//     {
//       Header: "Animal Count",
//       accessor: "animalCount",
//       disableSortBy: true,
//     },
//     {
//       Header: "Animal Fee",
//       accessor: "animalFee",
//       disableSortBy: true,
//     },
//     {
//       Header: "Animal Total Fee",
//       accessor: "totalFee",
//       disableSortBy: true,
//     },
//   ],
//   penalty: [
//     {
//       Header: "DEONAR_Penalty_Unit",
//       accessor: "unit",
//     },
//     {
//       Header: "ARRIVAL_Penalty_Amount",
//       accessor: "total",
//     },
//   ],

//   weighing: [
//     {
//       Header: "Animal Type",
//       accessor: "animal",
//       disableSortBy: true,
//     },
//     {
//       Header: "Animal unit",
//       accessor: "unit",
//       disableSortBy: true,
//     },
//     {
//       Header: "Weighing Charge",
//       accessor: "fee",
//       disableSortBy: true,
//     },
//     {
//       Header: "DEONAR_Skin_Unit",
//       accessor: "skinunit",
//     },
//     {
//       Header: "DEONAR_Skin_Fee",
//       accessor: "skinfee",
//     },
//     {
//       Header: "Weighing Subtotal",
//       accessor: "subtotal",
//       disableSortBy: true,
//     },
//     {
//       Header: "DEONAR_Weighing_Total",
//       accessor: "total",
//     },
//   ],
// }
  
 export const collectionDynamicColumns = (handleUUIDClick, tableType, t = (text) => text) => {
  if (typeof t !== "function") {
    console.warn("Translation function not provided, using default");
    t = (text) => text;
  }

  try {
    switch (tableType) {
      case "arrival":
        return [
          {
            Header: t("Animal Type"),
            accessor: "animalType",
            disableSortBy: true,
          },
          {
            Header: t("Animal Count"),
            accessor: "animalCount",
            disableSortBy: true,
          },
          {
            Header: t("Animal Fee"),
            accessor: "animalFee",
            disableSortBy: true,
          },
          {
            Header: t("Animal Total Fee"),
            accessor: "totalFee",
            disableSortBy: true,
          },
        ];
      case "stabling":
        return [
          {
            Header: t("Animal Type"),
            accessor: "animalType",
            disableSortBy: true,
          },
          {
            Header: t("Animal Count"),
            accessor: "animalCount",
            disableSortBy: true,
          },
          {
            Header: t("Total Stabling Fee"),
            accessor: "totalFee",
            disableSortBy: true,
          },
          {
            Header: t("Details"),
            disableSortBy: true,
          },
        ];
      case "washing":
        return [
          {
            Header: t("Vehicle Number"),
            accessor: "vehiclenumber",
            disableSortBy: true,
          },
          {
            Header: t("Vehicle Type"),
            accessor: "vehicletype",
            disableSortBy: true,
          },
          {
            Header: t("Total Washing Fee"),
            accessor: "total",
            disableSortBy: true,
          },
        ];
      case "parking":
        return [
          {
            Header: t("Vehicle Number"),
            accessor: "vehiclenumber",
            disableSortBy: true,
          },
          {
            Header: t("Vehicle Type"),
            accessor: "vehicletype",
            disableSortBy: true,
          },
          {
            Header: t("Parking Date"),
            accessor: "parkingdate",
            disableSortBy: true,
          },
          {
            Header: t("Parking Time"),
            accessor: "parkingtime",
            disableSortBy: true,
          },
          {
            Header: t("Departure Date"),
            accessor: "departuredate",
            disableSortBy: true,
          },
          {
            Header: t("Departure Time"),
            accessor: "departuretime",
            disableSortBy: true,
          },
          {
            Header: t("Total Hours"),
            accessor: "totalhours",
            disableSortBy: true,
          },
        ];
      case "slaughter":
        return [
          {
            Header: t("Animal"),
            accessor: "animalType",
            disableSortBy: true,
          },
          {
            Header: t("Animal Count"),
            accessor: "animalCount",
            disableSortBy: true,
          },
          {
            Header: t("Slaughter Fee"),
            accessor: "animalFee",
            disableSortBy: true,
          },
          {
            Header: t("Total Slaughter Fee"),
            accessor: "totalFee",
            disableSortBy: true,
          },
          {
            Header: t("Details"),
            disableSortBy: true,
          },
        ];
      case "removal":
        return [
          {
            Header: t("Animal Type"),
            accessor: "animalType",
            disableSortBy: true,
          },
          {
            Header: t("Animal Count"),
            accessor: "animalCount",
            disableSortBy: true,
          },
          {
            Header: t("Removal Fee"),
            accessor: "totalFee",
            disableSortBy: true,
          },
          {
            Header: t("Details"),
            disableSortBy: true,
          },
        ];
      case "trading":
        return [
          {
            Header: t("Animal Type"),
            accessor: "animalType",
            disableSortBy: true,
          },
          {
            Header: t("Animal Count"),
            accessor: "animalCount",
            disableSortBy: true,
          },
          {
            Header: t("Animal Fee"),
            accessor: "animalFee",
            disableSortBy: true,
          },
          {
            Header: t("Animal Total Fee"),
            accessor: "totalFee",
            disableSortBy: true,
          },
        ];
      case "penalty":
        return [
          {
            Header: t("DEONAR_Penalty_Unit"),
            accessor: "unit",
          },
          {
            Header: t("ARRIVAL_Penalty_Amount"),
            accessor: "total",
          },
        ];
      case "weighing":
        return [
          {
            Header: t("Animal Type"),
            accessor: "animal",
            disableSortBy: true,
          },
          {
            Header: t("Animal unit"),
            accessor: "unit",
            disableSortBy: true,
          },
          {
            Header: t("Weighing Charge"),
            accessor: "fee",
            disableSortBy: true,
          },
          {
            Header: t("DEONAR_Skin_Unit"),
            accessor: "skinunit",
          },
          {
            Header: t("DEONAR_Skin_Fee"),
            accessor: "skinfee",
          },
          {
            Header: t("Weighing Subtotal"),
            accessor: "subtotal",
            disableSortBy: true,
          },
          {
            Header: t("DEONAR_Weighing_Total"),
            accessor: "total",
          },
        ];
      default:
        console.warn("Unhandled table type in collectionDynamicColumns:", tableType);
        return [];
    }
  } catch (error) {
    console.error("Error in collectionDynamicColumns:", error);
    return [];
};
 }

export const toastMessages = {
  arrival: {
    success: "Entry Collection Fee saved successfully!",
    error: "Failed to save Entry Collection Fee.",
  },
  stabling: {
    success: "Stabling Fee saved successfully!",
    error: "Failed to save Stabling Fee.",
  },
  trading: {
    success: "Trading Fee saved successfully!",
    error: "Failed to save Trading Fee.",
  },
  removal: {
    success: "Removal Collection Fee saved successfully!",
    error: "Failed to save Removal Collection Fee.",
  },
  slaughter: {
    success: "Slaughter Recovery Fee saved successfully!",
    error: "Failed to save Slaughter Recovery Fee.",
  },
  washing: {
    success: "Vehicle Washing Fee saved successfully!",
    error: "Failed to save Vehicle Washing Fee.",
  },
  parking: {
    success: "Parking Fee saved successfully!",
    error: "Failed to save Parking Fee.",
  },
  weighing: {
    success: "Weighing Charge saved successfully!",
    error: "Failed to save Weighing Charge.",
  },
  penalty: {
    success: "Penalty Charge saved successfully!",
    error: "Failed to save Penalty Charge.",
  },
};

// Function to handle printing an element
export const handlePrint = (elementId) => {
  const element = document.getElementById(elementId);
  if (!element) {
    console.error("Element not found");
    return;
  }

  const printWindow = window.open("", "_blank");
  if (printWindow) {
    printWindow.document.write("<html><head><title>Print</title></head><body>");
    printWindow.document.write(element.outerHTML);
    printWindow.document.write("</body></html>");
    printWindow.document.close();
    printWindow.print();
  }
};

// Function to handle downloading an element as an HTML file
export const handleDownload = (elementId, fileName = "download.html") => {
  const element = document.getElementById(elementId);
  console.log("Element:", element); // Check if the element is found
  if (!element) {
    console.error("Element not found");
    return;
  }

  const htmlContent = element.outerHTML;
  const blob = new Blob([htmlContent], { type: "text/html" });
  const downloadLink = document.createElement("a");
  downloadLink.href = URL.createObjectURL(blob);
  downloadLink.download = fileName;
  document.body.appendChild(downloadLink);
  downloadLink.click();
  document.body.removeChild(downloadLink);
};


//generic functions for fee collection 

// utils/feeCollectionUtils.js

// Scroll handling utility
export const scrollToElementFee = (elementId, offset = -10) => {
  const scrollAttempts = [100, 300, 500];
  
  scrollAttempts.forEach((delay) => {
    setTimeout(() => {
      const element = document.getElementById(elementId);
      if (element) {
        try {
          const y = element.getBoundingClientRect().top + window.pageYOffset + offset;
          window.scrollTo({ top: y, behavior: "smooth" });
        } catch (error) {
          console.error(`Scroll attempt failed after ${delay}ms:`, error);
        }
      }
    }, delay);
  });
};

// PDF Generation utility
export const generatePDF = (data, downloadFileName, ReactDOMServer, ReceiptComponent, t) => {
  const printWindow = window.open("", "", "height=600,width=800");
  const receiptData = Array.isArray(data) ? data[0] : data;
  
  const htmlContent = `
    <!DOCTYPE html>
    <html>
      <head>
        <title>${downloadFileName}</title>
        <style>
          .receipt-container { max-width: 800px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif; }
          .receipt-header { text-align: center; margin-bottom: 30px; }
          .detail-row { display: flex; justify-content: space-between; margin-bottom: 10px; padding-bottom: 5px; border-bottom: 1px dotted #ddd; }
          .label { font-weight: bold; color: #555; }
          .value { text-align: right; }
          .animal-details { margin: 15px 0; padding: 10px; background-color: #f9f9f9; }
          .authorized-signature { margin-top: 50px; border-top: 1px solid #000; width: 200px; float: right; text-align: center; padding-top: 10px; }
          @media print { .no-print { display: none; } }
        </style>
      </head>
      <body>
        <div id="receipt-root"></div>
        <div class="no-print">
          <button onclick="window.print();window.close()" 
            style="display: block; width: 200px; margin: 20px auto; padding: 10px; 
            background-color: #4a90e2; color: white; border: none; border-radius: 4px; cursor: pointer;">
            Download Receipt
          </button>
        </div>
      </body>
    </html>
  `;

  printWindow.document.write(htmlContent);
  const receiptHtml = ReactDOMServer.renderToString(
    <ReceiptComponent receiptData={receiptData} t={t} />
  );
  printWindow.document.getElementById("receipt-root").innerHTML = receiptHtml;
  printWindow.document.close();
};

// Data formatting utility
export const formatFeeDataCollection = (data, type) => {
  const formatters = {
    arrival: (details) => details?.Details?.flatMap(item =>
      item.details.map(detail => ({
        animalType: detail.animal,
        animalCount: detail.count,
        animalFee: detail?.stableFeeDetails[0]?.fee_with_stakeholder,
        totalFee: detail.totalFee,
        total: item.total,
      }))
    ),

    stabling: (data) => data?.Details?.flatMap(item =>
      item.details.map(detail => ({
        animalType: detail.animal,
        animalCount: detail.count,
        totalFee: detail.totalFee,
        total: item.total,
        stableFeeDetails: detail.stableFeeDetails?.map(feeDetail => ({
          token: feeDetail.token,
          animalTypeId: feeDetail.animaltypeid,
          daysWithStakeholder: feeDetail.days_with_stakeholder,
          feeWithStakeholder: feeDetail.fee_with_stakeholder,
        })) || [],
      }))
    ),

    washing: (data) => {
      const washingData = data?.VehicleVehicleWashingFeesResponse;
      return washingData ? [{
        vehiclenumber: washingData.vehicleNumber,
        vehicletype: washingData.vehicleType,
        washingTime: washingData.washingTime || "N/A",
        washingDate: washingData.washingDate,
        total: washingData.washingFee,
      }] : [];
    },

    parking: (data, parkingDetails) => {
      if (!data?.vehicleParkingFeeResponseDetails) return [];
      
      const now = new Date();
      const currentDate = now.toISOString().split("T")[0];
      const currentTime = now.toTimeString().split(" ")[0];

      const calculateHours = (parkingDate, parkingTime, departureDate, departureTime) => {
        if (!parkingDate || !parkingTime) return 0;
        const parkingDateTime = new Date(`${parkingDate}T${parkingTime}`);
        const departureDateTime = new Date(`${departureDate}T${departureTime}`);
        const diffMs = departureDateTime - parkingDateTime;
        const hours = Math.round((diffMs / (1000 * 60 * 60)) * 100) / 100;
        return hours > 0 ? hours : 0;
      };

      return data.vehicleParkingFeeResponseDetails.map(data => {
        const matchingParkingDetail = parkingDetails?.find(detail => 
          detail.vehicleNumber === data.vehicleNumber
        );
        const totalHours = calculateHours(
          matchingParkingDetail?.parkingDate,
          matchingParkingDetail?.parkingTime,
          currentDate,
          currentTime
        );

        return {
          vehiclenumber: data.vehicleNumber,
          vehicletype: data.vehicleType,
          parkingdate: matchingParkingDetail?.parkingDate || data.parkingdate,
          parkingtime: matchingParkingDetail?.parkingTime || data.parkingtime,
          departuredate: currentDate,
          departuretime: currentTime,
          totalhours: totalHours || data.totalhours,
          total: data.parkingFee,
        };
      });
    },

    slaughter: (data) => data?.Details?.flatMap(item =>
      item?.details?.map(detail => ({
        animalType: detail?.animal,
        animalCount: detail?.count,
        animalFee: detail?.stableFeeDetails[0]?.fee_with_stakeholder,
        totalFee: detail?.totalFee,
        total: item?.total,
        stableFeeDetails: detail.stableFeeDetails?.map(feeDetail => ({
          token: feeDetail.token,
          animalTypeId: feeDetail.animaltypeid,
          daysWithStakeholder: feeDetail.days_with_stakeholder,
          feeWithStakeholder: feeDetail.fee_with_stakeholder,
        })) || [],
      }))
    ),

    removal: (data) => data?.removalDetails?.flatMap(item =>
      item?.details?.map(detail => ({
        animalType: detail.animal,
        animalCount: detail.count,
        totalFee: detail.totalFee,
        total: item.total,
        stableFeeDetails: detail.stableFeeDetails?.map(feeDetail => ({
          token: feeDetail.token,
          animalTypeId: feeDetail.animaltypeid,
          daysWithStakeholder: feeDetail.days_with_stakeholder,
          feeWithStakeholder: feeDetail.fee_with_stakeholder,
          removalType: feeDetail?.removal_type,
        })) || [],
      }))
    ),

    trading: (data) => data?.Details?.flatMap(item =>
      item?.details?.map(detail => ({
        animalType: detail?.animal,
        animalCount: detail?.count,
        animalFee: detail?.fee,
        totalFee: detail?.totalFee,
        total: item?.total,
      }))
    ),

    penalty: (data, selectedUUID) => {
      if (!data?.PenaltyLists) return [];
      return data.PenaltyLists
        .filter(item => item.penaltyReference === selectedUUID)
        .map(value => ({
          total: value.total,
          unit: value.unit === null ? 1 : value.unit,
        }));
    },

    weighing: (data) => data?.Details?.flatMap(item =>
      item?.details?.map(detail => ({
        animal: detail?.animal,
        unit: detail?.unit,
        fee: detail?.fee,
        subtotal: detail?.subtotal,
        skinunit: detail?.skinunit,
        skinfee: detail?.skinfee,
        total: item?.total,
      }))
    ),
  };

  return formatters[type] ? formatters[type](data) : [];
};

// Generic API payload generator
export const generateFeePayload = (type, data, additionalData = {}) => {
  const payloadGenerators = {
    arrival: (formattedData) => {
      const details = formattedData.map(item => ({
        animal: item.animalType,
        count: item.animalCount,
        fee: item.animalFee,
        totalFee: item.totalFee
      }));

      return {
        Details: [{
          details,
          total: formattedData[0]?.total || 0
        }]
      };
    },

    stabling: (formattedData) => {
      const details = formattedData.map(item => ({
        animal: item.animalType,
        count: item.animalCount,
        totalFee: item.totalFee,
        stableFeeDetails: item.stableFeeDetails.map(detail => ({
          token: detail.token,
          animaltypeid: detail.animalTypeId,
          days_with_stakeholder: detail.daysWithStakeholder,
          fee_with_stakeholder: detail.feeWithStakeholder
        }))
      }));

      return {
        Details: [{
          details,
          total: formattedData[0]?.total || 0
        }]
      };
    },

    washing: (formattedData) => ({
      VehicleVehicleWashingFeesResponse: {
        vehicleNumber: formattedData[0]?.vehiclenumber,
        vehicleType: formattedData[0]?.vehicletype,
        washingTime: formattedData[0]?.washingTime,
        washingDate: formattedData[0]?.washingDate,
        washingFee: formattedData[0]?.total
      }
    }),

    parking: (formattedData) => ({
      vehicleParkingFeeResponseDetails: formattedData.map(item => ({
        vehicleNumber: item.vehiclenumber,
        vehicleType: item.vehicletype,
        parkingdate: item.parkingdate,
        parkingtime: item.parkingtime,
        departuredate: item.departuredate,
        departuretime: item.departuretime,
        totalhours: item.totalhours,
        parkingFee: item.total
      }))
    }),

    slaughter: (formattedData) => {
      const details = formattedData.map(item => ({
        animal: item.animalType,
        count: item.animalCount,
        totalFee: item.totalFee,
        stableFeeDetails: item.stableFeeDetails.map(detail => ({
          token: detail.token,
          animaltypeid: detail.animalTypeId,
          days_with_stakeholder: detail.daysWithStakeholder,
          fee_with_stakeholder: detail.feeWithStakeholder
        }))
      }));

      return {
        Details: [{
          details,
          total: formattedData[0]?.total || 0
        }]
      };
    },

    removal: (formattedData) => {
      const details = formattedData.map(item => ({
        animal: item.animalType,
        count: item.animalCount,
        totalFee: item.totalFee,
        stableFeeDetails: item.stableFeeDetails.map(detail => ({
          token: detail.token,
          animaltypeid: detail.animalTypeId,
          days_with_stakeholder: detail.daysWithStakeholder,
          fee_with_stakeholder: detail.feeWithStakeholder,
          removal_type: detail.removalType
        }))
      }));

      return {
        removalDetails: [{
          details,
          total: formattedData[0]?.total || 0
        }]
      };
    },

    trading: (formattedData) => {
      const details = formattedData.map(item => ({
        animal: item.animalType,
        count: item.animalCount,
        fee: item.animalFee,
        totalFee: item.totalFee
      }));

      return {
        Details: [{
          details,
          total: formattedData[0]?.total || 0
        }]
      };
    },

    penalty: (formattedData, { penaltyReference }) => ({
      PenaltyLists: formattedData.map(item => ({
        penaltyReference,
        total: item.total,
        unit: item.unit
      }))
    }),

    weighing: (formattedData) => {
      const details = formattedData.map(item => ({
        animal: item.animal,
        unit: item.unit,
        fee: item.fee,
        subtotal: item.subtotal,
        skinunit: item.skinunit,
        skinfee: item.skinfee
      }));

      return {
        Details: [{
          details,
          total: formattedData[0]?.total || 0
        }]
      };
    }
  };

  const generator = payloadGenerators[type];
  if (!generator) return null;

  return generator(data, additionalData);
};

// Cache utility for handling data persistence
export const createDataCache = () => {
  const cache = new Map();
  
  return {
    getData: (key) => cache.get(key),
    setData: (key, data) => cache.set(key, data),
    hasData: (key) => cache.has(key),
    clearData: (key) => cache.delete(key),
    clearAll: () => cache.clear(),
  };
};