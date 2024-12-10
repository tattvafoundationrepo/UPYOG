import { AddIcon, RemoveIcon, Dropdown, TextInput } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";

const GenericAddRow = ({
  maxAllowedCount,
  allowEdit = true,
  allowRemove = true,
  label = "",
  dropdownLabel = "Dropdown",
  inputLabel = "Input",
  options = [],
  fieldNames = { dropdown: "dropdownField", input: "inputField" },
  onRowsUpdate,
  dynamicOptionKey = "label", // New prop for dynamic option key
}) => {
  const { t } = useTranslation();

  const [rows, setRows] = useState([]);
  const [currentRow, setCurrentRow] = useState({ dropdownValue: "", inputValue: "" });

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm();
  const [isEditable, setIsEditable] = useState(allowEdit);

  const validateAnimalCount = (currentTotal, maxAllowed) => {
    if (currentTotal > maxAllowed) {
      return `Please check the animal count. You can only add up to ${maxAllowed} animals.`;
    }
    return null;
  };

  const isDuplicateRow = () => {
    return rows.some(
      (row) =>
        row.dropdownValue[dynamicOptionKey] === currentRow.dropdownValue[dynamicOptionKey] &&
        row.inputValue === currentRow.inputValue
    );
  };
  

  const handleAddRow = () => {
    if (!currentRow.dropdownValue || !currentRow.inputValue) {
      alert("Please select a value from the dropdown and enter a valid number.");
      return;
    }
  
    if (isDuplicateRow()) {
      alert("This value has already been added. Please select a different option or input value.");
      return;
    }
  
    const totalAnimals = rows.reduce((sum, row) => sum + parseInt(row.inputValue), 0) + parseInt(currentRow.inputValue);
    const validationError = validateAnimalCount(totalAnimals, maxAllowedCount);
    if (validationError) {
      alert(validationError);
      return;
    }
  
    setRows((prevRows) => [...prevRows, currentRow]);
    setCurrentRow({ dropdownValue: "", inputValue: "" });
    onRowsUpdate([...rows, currentRow]);
  };
  
  

  const handleRemoveRow = (index) => {
    const updatedRows = rows.filter((row, rowIndex) => rowIndex !== index);
    setRows(updatedRows);
    onRowsUpdate(updatedRows);
  };

  return (
    <div>
      <div className="bmc-title">{t(label)}</div>
      <div className="bmc-table-container" style={{ padding: "1rem" }}>
        <table className="bmc-hover-table">
          <thead>
            <tr>
              <th scope="col">{t(dropdownLabel)}</th>
              <th scope="col">{t(inputLabel)}</th>
              {allowRemove && <th scope="col"></th>}
            </tr>
          </thead>
          <tbody>
            <tr>
              <td data-label={t("Dropdown")} style={{ textAlign: "left", paddingTop: "16px" }}>
                <Controller
                  control={control}
                  name="dropdown"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      {isEditable ? (
                        <Dropdown
                          placeholder={t("SELECT_OPTION")}
                          selected={currentRow.dropdownValue}
                          select={(e) => setCurrentRow({ ...currentRow, dropdownValue: e })}
                          onBlur={props.onBlur}
                          optionKey={dynamicOptionKey} // Use dynamic key here
                          t={t}
                          option={options}
                          isMandatory={true}
                          className="employee-select-wrap bmc-form-field"
                        />
                      ) : (
                        <TextInput disabled={!isEditable} readOnly={!isEditable} value={currentRow.dropdownValue?.[dynamicOptionKey] || ""} />
                      )}
                    </div>
                  )}
                />
              </td>
              <td data-label={t("Input")} style={{ textAlign: "left" }}>
                <Controller
                  control={control}
                  name="input"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <TextInput
                        placeholder={t("Enter Value")}
                        value={currentRow.inputValue}
                        onChange={(e) => setCurrentRow({ ...currentRow, inputValue: e.target.value })}
                        onBlur={props.onBlur}
                        t={t}
                      />
                      {errors.ifsc && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.ifsc.message}</sup>}
                    </div>
                  )}
                />
              </td>
              <td data-label={t("Add Row")}>
                <button type="button" onClick={handleAddRow} style={{ paddingBottom: "18px", outline: "none" }}>
                  <AddIcon className="bmc-add-icon" />
                </button>
              </td>
            </tr>
            {rows.map((row, index) => (
              <tr key={index}>
                <td>{row.dropdownValue[dynamicOptionKey]}</td>
                <td>{row.inputValue}</td>
                {allowRemove && (
                  <td data-label={t("Remove Row")}>
                    <button type="button" onClick={() => handleRemoveRow(index)}>
                      <RemoveIcon className="bmc-remove-icon" />
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default GenericAddRow;
