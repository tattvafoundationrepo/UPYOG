import { AddIcon, Dropdown, TextInput } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import useDeonarCommon from "../../../../../../libraries/src/hooks/deonar/useCommonDeonar";

const BrokerNameField = (AllowEdit = true, AllowRemove = true) => {
  const [isEditable, setIsEditable] = useState(AllowEdit);
  const [options, setOptions] = useState([]);
  const { t } = useTranslation();

  const { fetchDeonarCommon } = useDeonarCommon();

  const { data: commonApiData, error: apiError, isLoading: apiLoading } = fetchDeonarCommon({
    CommonSearchCriteria: {
      Option: "broker",
    },
  });

  const staticOptions = [
    { name: "Broker 1", value: "1" },
    { name: "Broker 2", value: "2" },
    { name: "Broker 3", value: "3" },
  ];

  useEffect(() => {
    setOptions(staticOptions);
  }, []);

  // useEffect(() => {
  //   if (commonApiData && Array.isArray(commonApiData.CommonDetails)) {
  //     const names = commonApiData.CommonDetails.map(item => ({
  //       name: item.name,
  //       value: item.id
  //     }));
  //     setOptions(names);
  //   } else {
  //     setOptions([]);
  //   }
  // }, [commonApiData]);
  
  const {
    control,
    handleSubmit,
    reset,
    getValues,
    setValue,
    watch,
    formState: { errors },
  } = useForm();

  return (
    <div>
      <div className="bmc-title">{t("BROKER_NAME")}</div>
      <div className="bmc-table-container" style={{ padding: "1rem" }}>
          <table className="bmc-hover-table">
            <thead>
              <tr>
                <th scope="col">{t("DEONAR_BROKER_NAME")}</th>
                <th scope="col">{t("Animal Count")}</th>
                {AllowRemove && <th scope="col"></th>}
              </tr>
            </thead>
            <tbody>
              <tr>
                <td data-label={t("Gawal Count")} style={{ textAlign: "left", paddingTop: "16px"  }}>
                  <Controller
                    control={control}
                    name="animal"
                    rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                    render={(props) => (
                      <div>
                        {isEditable ? (
                          <Dropdown
                            placeholder={t("SELECT DEONAR_BROKER_NAME")}
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
                  />
                </td>
                <td data-label={t("Gawal Type")} style={{ textAlign: "left" }}>
                  <Controller
                    control={control}
                    name="animalType"
                    rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                    render={(props) => (
                      <div>
                        <TextInput placeholder={t("Total Animals")} value={props.value} onChange={props.onChange} onBlur={props.onBlur} t={t} />
                        {errors.ifsc && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.ifsc.message}</sup>}
                      </div>
                    )}
                  />
                </td>
                <td data-label={t("ANIMAL_ADD_ROW")}>
                  <button type="button" style={{ paddingBottom: "18px" }}>
                    <AddIcon className="bmc-add-icon" />
                  </button>
                </td>
              </tr>

              {rows.map((row, index) => (
              <tr key={index}>
                <td data-label={t("Gawal Name")}>{row?.brokername}</td>
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

export default BrokerNameField;

