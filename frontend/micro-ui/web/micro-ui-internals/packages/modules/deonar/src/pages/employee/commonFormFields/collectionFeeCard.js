import React, { useState } from "react";
import { Controller } from "react-hook-form";
import { Dropdown, TextInput } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import CustomTable from "../commonFormFields/customTable";
import { generateTokenNumber } from "../collectionPoint/utils";

const CollectionFeeCard = ({
  label = "",
  fields = [],
  options = {},
  control,
  allowEdit = true,
  showTable = false,
  tableData = [],
  Tablecolumns = [],
  isLoading = false,
  onFieldChange, // Add onFieldChange prop
  feeType = {},
}) => {
  const { t } = useTranslation();
  const [isTableVisible, setIsTableVisible] = useState(false);

  const handleCellClick = (column, row) => {
    if ((feeType === "stabling" || feeType === "removal" || feeType === "slaughter") && column.Header === "Details") {
      setIsTableVisible(!isTableVisible);
    }
  };

  const mappedTableData = tableData?.flatMap(
    (data) =>
      data.stableFeeDetails?.map((feeDetail) => ({
        animalType: data.animalType,
        token: feeDetail.token,
        animalTypeId: feeDetail.animalTypeId,
        daysWithStakeholder: feeDetail.daysWithStakeholder,
        feeWithStakeholder: feeDetail.feeWithStakeholder,
        count: feeDetail.token,
        removalType: feeDetail?.removalType,
      })) || []
  );

  const columnsMapping = {
    stabling: [
      { Header: t("Animal Type"), accessor: "animalType" },
      {
        Header: t("Animal Token"),
        accessor: "count",
        Cell: ({ row }) => {
          const animalType = row.original.animalType;
          return generateTokenNumber(animalType, row.original.count);
        },
      },
      {
        Header: t("Day's"),
        accessor: "daysWithStakeholder",
      },
      {
        Header: t("Amount"),
        accessor: "feeWithStakeholder",
      },
    ],
    removal: [
      { Header: t("Animal Type"), accessor: "animalType" },
      {
        Header: t("Animal Token"),
        accessor: "count",
        Cell: ({ row }) => {
          const animalType = row.original.animalType;
          return generateTokenNumber(animalType, row.original.count);
        },
      },
      {
        Header: t("Day's"),
        accessor: "daysWithStakeholder",
      },
      {
        Header: t("Amount"),
        accessor: "feeWithStakeholder",
      },
      {
        Header: t("Removal Type"),
        accessor: "removalType",
      },
    ],
    slaughter: [
      { Header: t("Animal Type"), accessor: "animalType" },
      {
        Header: t("Animal Token"),
        accessor: "count",
        Cell: ({ row }) => {
          const animalType = row.original.animalType;
          return generateTokenNumber(animalType, row.original.count);
        },
      },
      {
        Header: t("Day's"),
        accessor: "daysWithStakeholder",
      },
      {
        Header: t("Amount"),
        accessor: "feeWithStakeholder",
      },
    ],
  };

  const tableColumns = columnsMapping[feeType];

  return (
    <div>
      <div className="bmc-title">{t(label)}</div>
      <div className="bmc-table-container" style={{ padding: "1rem" }}>
        <div style={{ justifyContent: "center", gap: "20px" }}>
          {showTable && (
            <div style={{ marginBottom: "40px", width: "100%" }}>
              <CustomTable
                t={t}
                columns={Tablecolumns.map((column) => ({
                  ...column,
                  Cell: (cellInfo) => {
                    if (column.Header === "Details") {
                      return <span onClick={() => handleCellClick(column, cellInfo.row.original)}>{isTableVisible ? t("Hide") : t("Show")}</span>;
                    }
                    return (
                      <div onClick={() => handleCellClick(column, cellInfo.row.original)} style={{ cursor: "pointer" }}>
                        {cellInfo.value}
                      </div>
                    );
                  },
                }))}
                // tableClassName="deonar-scrollable-table"
                data={tableData}
                disableSort={false}
                autoSort={false}
                manualPagination={false}
                showSearch={false}
                showTotalRecords={false}
                showPagination={false}
                isLoadingRows={isLoading}
                getCellProps={(cellInfo) => ({
                  style: { fontSize: "16px" },
                })}
              />
            </div>
          )}
          {isTableVisible && (
            <React.Fragment>
              <span>{t("Description of Animal")}</span>
              <CustomTable
                t={t}
                columns={tableColumns}
                // tableClassName="deonar-scrollable-table"
                data={mappedTableData}
                disableSort={false}
                autoSort={false}
                manualPagination={false}
                showSearch={false}
                showTotalRecords={true}
                showPagination={true}
                isLoadingRows={isLoading}
                getCellProps={(cellInfo) => ({
                  style: { fontSize: "16px" },
                })}
              />
            </React.Fragment>
          )}
          {/* <div className="bmc-col3-card" style={{ width: "100%" }}> */}
          {fields.map((field, index) => (
            <React.Fragment>
              <div className="bmc-col3-card">
                <div key={index} className={`bmc-form-field ${field.width || "full"}`}>
                  <label>{t(field.label)}</label>
                  <Controller
                    control={control}
                    name={field.name}
                    rules={{ required: field.required }}
                    render={({ field: controllerField = {} }) => {
                      const handleChange = (value) => {
                        if (controllerField.onChange) {
                          controllerField.onChange(value); // Update react-hook-form's state
                        }
                        if (onFieldChange) {
                          onFieldChange(field.name, value); // Update parent's formData state
                        }
                      };

                      return field.type === "dropdown" ? (
                        <Dropdown
                          option={options[field.name] || []}
                          selected={controllerField.value}
                          select={handleChange}
                          t={t}
                          optionKey="label"
                          onBlur={controllerField.onBlur}
                          disable={!allowEdit}
                        />
                      ) : (
                        <TextInput
                          {...controllerField}
                          onChange={(e) => handleChange(e.target.value)}
                          ref={controllerField.ref}
                          onBlur={controllerField.onBlur}
                          disable={!allowEdit}
                        />
                      );
                    }}
                  />
                </div>
              </div>
            </React.Fragment>
          ))}
        </div>
        {/* </div> */}
      </div>
    </div>
  );
};

export default CollectionFeeCard;
