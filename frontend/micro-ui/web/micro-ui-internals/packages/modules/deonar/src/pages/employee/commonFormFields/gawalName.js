import { AddIcon, Dropdown, RemoveIcon, TextInput } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import useDeonarCommon from "../../../../../../libraries/src/hooks/deonar/useCommonDeonar";

const GawalNameField = ({ initialRows = [], AllowEdit = true, AllowRemove = true, AddOption = true, onUpdate }) => {
  const [isEditable, setIsEditable] = useState(AllowEdit);
  const [options, setOptions] = useState([]);
  const [rows, setRows] = useState(initialRows);
  const { t } = useTranslation();

  const { fetchDeonarCommon } = useDeonarCommon();

  const { data: commonApiData } = fetchDeonarCommon({
    CommonSearchCriteria: {
      Option: "animal",
    },
  });

  const staticOptions = [
    { name: "Gawal 1", value: "1" },
    { name: "Gawal 2", value: "2" },
    { name: "Gawal 3", value: "3" },
  ];

  useEffect(() => {
    setOptions(staticOptions);
  }, []);

  const {
    control,
    getValues,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: {
      gawalName: null,
      totalCount: "",
    },
  });

  const handleAddRow = () => {
    const formData = getValues();
    const selectedGawal = options.find((option) => option.value === formData.gawalName);
    if (!selectedGawal) {
      console.error("Selected animal type is not found in options");
      return;
    }

    const updatedRows = [
      ...rows,
      {
        gawalName: selectedGawal.name,
        totalCount: formData.totalCount,
      },
    ];
    setRows(updatedRows);
    reset({ gawalName: null, totalCount: "" });
    if (onUpdate) onUpdate(updatedRows);
  };

  const handleRemoveRow = (index) => {
    const updatedRows = [...rows];
    updatedRows.splice(index, 1);
    setRows(updatedRows);
    if (onUpdate) onUpdate(updatedRows);
  };

  return (
    <div>
      <div className="bmc-title">{t("GAWAL_NAME")}</div>
      <div className="bmc-table-container" style={{ padding: "1rem" }}>
        <table className="bmc-hover-table">
          <thead>
            <tr>
              <th scope="col">{t("Gawal Name")}</th>
              <th scope="col">{t("Animal Count")}</th>
              {AllowRemove && <th scope="col">{t("Action")}</th>}
            </tr>
          </thead>
          <tbody>
            {AddOption && (
              <tr>
                <td data-label={t("Animal Type")} style={{ textAlign: "left", paddingTop: "16px" }}>
                  <Controller
                    control={control}
                    name="gawalType"
                    rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                    render={(props) => (
                      <div>
                        {isEditable ? (
                          <Dropdown
                            placeholder={t("Select Gawal Name")}
                            selected={props.value}
                            select={props.onChange}
                            onBlur={props.onBlur}
                            optionKey="name"
                            t={t}
                            option={options}
                            isMandatory={true}
                            className="employee-select-wrap bmc-form-field"
                          />
                        ) : (
                          <TextInput disabled={!isEditable} readOnly={!isEditable} value={props.value?.name || ""} />
                        )}
                      </div>
                    )}
                    defaultValue={null}
                  />
                </td>
                <td data-label={t("Animal Count")} style={{ textAlign: "left" }}>
                  <Controller
                    control={control}
                    name="totalCount"
                    rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                    render={(props) => (
                      <div>
                        <TextInput placeholder={t("Total Animals")} value={props.value || ""} onChange={props.onChange} onBlur={props.onBlur} t={t} />
                        {errors.totalCount && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.totalCount.message}</sup>}
                      </div>
                    )}
                    defaultValue=""
                  />
                </td>
                <td data-label={t("ANIMAL_ADD_ROW")}>
                  <button type="button" onClick={handleAddRow} style={{ paddingBottom: "18px" }}>
                    <AddIcon className="bmc-add-icon" />
                  </button>
                </td>
              </tr>
            )}
            {rows.map((row, index) => (
              <tr key={index}>
                <td data-label={t("Gawal Name")}>{row?.gawalName}</td>
                <td data-label={t("Animal Count")}>{row?.totalCount}</td>
                {AllowRemove && (
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

export default GawalNameField;


