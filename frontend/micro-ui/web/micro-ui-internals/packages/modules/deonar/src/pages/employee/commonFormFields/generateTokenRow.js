import { AddIcon, RemoveIcon, Dropdown, TextInput, LabelFieldPair, CardLabel } from "@upyog/digit-ui-react-components";
import React, { useState, useEffect } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";

const DynamicRowGenerator = ({
  maxAllowedCount,
  allowEdit = true,
  allowRemove = true,
  label = "Label",
  dropdownLabel = "Dropdown",
  inputLabel = "Input",
  generateLabel = "Generate",
  options = [],
  fieldNames = { dropdown: "dropdownField", input: "inputField" },
  onRowsUpdate,
  dynamicOptionKey = "label",
  showMappedRows = true,
  generateButtonLabel = "Generate",
  handleGenerateRows: externalHandleGenerateRows,
  handleAddRow: externalHandleAddRow,
  showAddIcon = true,
  message = "Action completed successfully", // New prop for message
  messageDuration = 3000, // New prop for message duration (in milliseconds)
}) => {
  const { t } = useTranslation();
  const [rows, setRows] = useState([]);
  const [currentRow, setCurrentRow] = useState({ dropdownValue: "", inputValue: "" });
  const [showMessage, setShowMessage] = useState(false); // State for controlling the message chip

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
      (row) => row.dropdownValue[dynamicOptionKey] === currentRow.dropdownValue[dynamicOptionKey] && row.inputValue === currentRow.inputValue
    );
  };

  const defaultHandleAddRow = () => {
    if (!currentRow.dropdownValue || !currentRow.inputValue) {
      setShowMessage(true); // Show the message
      setTimeout(() => setShowMessage(false), messageDuration); // Hide the message after the duration
      return;
    }

    if (isDuplicateRow()) {
      setShowMessage(true);
      setTimeout(() => setShowMessage(false), messageDuration);
      return;
    }

    const totalAnimals = rows.reduce((sum, row) => sum + parseInt(row.inputValue), 0) + parseInt(currentRow.inputValue);
    const validationError = validateAnimalCount(totalAnimals, maxAllowedCount);
    if (validationError) {
      setShowMessage(true);
      setTimeout(() => setShowMessage(false), messageDuration);
      return;
    }

    const newRows = [...rows, currentRow];
    setRows(newRows);
    setCurrentRow({ dropdownValue: "", inputValue: "" });
    onRowsUpdate(newRows);
  };

  const defaultHandleGenerateRows = () => {
    const inputValue = parseInt(currentRow.inputValue);
    if (!currentRow.dropdownValue) {
      setShowMessage(true);
      setTimeout(() => setShowMessage(false), messageDuration);
      return;
    }

    if (isNaN(inputValue) || inputValue <= 0) {
      setShowMessage(true);
      setTimeout(() => setShowMessage(false), messageDuration);
      return;
    }

    const newRows = Array.from({ length: inputValue }, (_, index) => ({
      dropdownValue: currentRow.dropdownValue,
      inputValue: index + 1,
    }));

    const totalRows = [...rows, ...newRows];
    const totalAnimals = totalRows.reduce((sum, row) => sum + row.inputValue, 0);

    if (validateAnimalCount(totalAnimals, maxAllowedCount)) {
      setShowMessage(true);
      setTimeout(() => setShowMessage(false), messageDuration);
      return;
    }

    setRows(totalRows);
    setCurrentRow({ dropdownValue: "", inputValue: "" });
    onRowsUpdate(totalRows);

    if (externalHandleGenerateRows) {
      externalHandleGenerateRows(currentRow, rows, setRows, setCurrentRow, onRowsUpdate, setShowMessage);
    }
  };

  const handleAddRow = externalHandleAddRow || defaultHandleAddRow;
  const handleGenerateRows = externalHandleGenerateRows || defaultHandleGenerateRows;

  const handleRemoveRow = (index) => {
    const updatedRows = rows.filter((_, rowIndex) => rowIndex !== index);
    setRows(updatedRows);
    onRowsUpdate(updatedRows);
  };

  return (
    <div>
      {/* <div className="bmc-title">{t(label)}</div> */}
      {showMessage && (
        <div className="chip" style={{ background: "#d1e7dd", padding: "8px", marginBottom: "10px", borderRadius: "5px" }}>
          <span style={{ color: "#0f5132" }}>{t(message)}</span>
        </div>
      )}
      {/* <div className="bmc-table-container" style={{ padding: "1rem" }}>
        <table className="bmc-hover-table">
          <thead>
            <tr>
              <th scope="col">{t(dropdownLabel)}</th>
              <th scope="col">{t(inputLabel)}</th>
              <th scope="col">{t(generateLabel)}</th>
              {allowRemove && <th scope="col"></th>}
            </tr>
          </thead> */}
      <div className="bmc-card-row">
        <div className="bmc-col2-card">
        <LabelFieldPair>
        <CardLabel className="bmc-label">{t("Animal Type")}</CardLabel>
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
                    optionKey={dynamicOptionKey}
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
          </LabelFieldPair>
        </div>
        <div className="bmc-col2-card">
        <LabelFieldPair>
        <CardLabel className="bmc-label">{t("Animal Count")}</CardLabel>
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
                {errors.input && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.input.message}</sup>}
              </div>
            )}
          />
          </LabelFieldPair>
        </div>
      </div>
      <button
        type="button"
        onClick={() => handleGenerateRows(currentRow, rows, setRows, setCurrentRow, onRowsUpdate)}
        style={{
          float: "right",
          padding: "2px 20px",
          outline: "none",
          background: "#f47738",
          borderRadius: "15px",
          marginBottom: "14px",
          color: "white",
        }}
      >
        {t(generateButtonLabel)}
      </button>
      {showAddIcon && (
        <td data-label={t("Add Row")}>
          <button
            type="button"
            onClick={() => handleAddRow(currentRow, rows, setRows, setCurrentRow, onRowsUpdate)}
            style={{ paddingBottom: "18px", outline: "none" }}
          >
            <AddIcon className="bmc-add-icon" />
          </button>
        </td>
      )}
      {showMappedRows &&
        rows.map((row, index) => (
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
      {/* </table>
      </div> */}
    </div>
  );
};

export default DynamicRowGenerator;
