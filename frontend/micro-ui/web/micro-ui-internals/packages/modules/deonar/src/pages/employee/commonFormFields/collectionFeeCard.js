import React from "react";
import { Controller } from "react-hook-form";
import { Dropdown, TextInput } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import CustomTable from "../commonFormFields/customTable";

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
}) => {
  const { t } = useTranslation();

  return (
    <div>
      <div className="bmc-title">{t(label)}</div>
      <div className="bmc-table-container" style={{ padding: "1rem", minHeight: "318px" }}>
        {showTable && (
          <div style={{ marginBottom: "40px" }}>
            <CustomTable
              t={t}
              columns={Tablecolumns}
              tableClassName="deonar-scrollable-table"
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
        <div className="bmc-form-grid">
          {fields.map((field, index) => (
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
          ))}
        </div>
      </div>
    </div>
  );
};

export default CollectionFeeCard;
