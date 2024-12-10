import { AddIcon, Dropdown, RemoveIcon, TextInput } from "@upyog/digit-ui-react-components";
import React, { useEffect, useMemo, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import useDeonarCommon from "../../../../../../libraries/src/hooks/deonar/useCommonDeonar";

const NumberOfAnimalCard = ({ initialRows = [], AllowEdit = true, AllowRemove = true, AddOption = true, onUpdate, addRow, removeRow }) => {
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

  useEffect(() => {
    if (commonApiData && Array.isArray(commonApiData.CommonDetails)) {
      const names = commonApiData.CommonDetails.map((item) => ({
        name: item.name,
        value: item.id,
      }));
      setOptions(names);
    } else {
      setOptions([]);
    }
  }, [commonApiData]);

  const {
    control,
    getValues,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: {
      animalType: "",
      totalCount: "",
    },
  });

  const handleAddRow = () => {
    const formData = getValues();
    const selectedAnimal = options.find((option) => option.value === formData.animalType.value);
    if (!selectedAnimal) {
      console.error("Selected animal type is not found in options");
      return;
    }
    const updatedRows = [
      ...rows,
      {
        animalId: selectedAnimal.value,
        animalType: selectedAnimal.name,
        totalCount: formData.totalCount,
      },
    ];
    setRows(updatedRows);
    reset();
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
      <div className="bmc-title">{t("NUMBER_Of_ANIMALS")}</div>
      <div className="bmc-table-container" style={{ padding: "1rem" }}>
        <table className="bmc-hover-table">
          <thead>
            <tr>
              <th scope="col">{t("Animal Type")}</th>
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
                    name="animalType"
                    rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                    render={({ value, onChange, onBlur }) => (
                      <div>
                        {isEditable ? (
                          <Dropdown
                            placeholder={t("SELECT ANIMAL")}
                            selected={value}
                            select={onChange}
                            onBlur={onBlur}
                            optionKey="name"
                            t={t}
                            option={options}
                            isMandatory={true}
                            className="employee-select-wrap bmc-form-field"
                          />
                        ) : (
                          <TextInput disabled={!isEditable} readOnly={!isEditable} value={value?.name || ""} />
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
                    render={({ value, onChange, onBlur }) => (
                      <div>
                        <TextInput placeholder={t("Total Animals")} value={value || ""} onChange={onChange} onBlur={onBlur} t={t} />
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
                <td style={{ display: "none" }}>{row?.animalId}</td>
                <td data-label={t("Animal Type")}>{row?.animalType}</td>
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

export default NumberOfAnimalCard;
