import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";

const VehicleTypeDropdownField = ({ control, data, setData }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");
  const [options, setOptions] = useState([]);

  const { fetchDeonarCommon } = useDeonarCommon();
  const useFetchOptions = (optionType) => {
    const { data } = fetchDeonarCommon({
      CommonSearchCriteria: {
        Option: optionType,
      },
    });

    return data ? data.CommonDetails.map((item) => ({
      name: item.name,
      value: item.id,
    })) : [];
  };

  const vechicleData = useFetchOptions("vehicle");

  useEffect(() => {
    setOptions(vechicleData.map((item) => item.name || []));
  }, []);

  return (
    // <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">
          {t("DEONAR_VEHICLE_TYPE")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}
        </CardLabel>
        <Controller
          control={control}
          name="vehicleType"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={(props) => (
            <Dropdown
              name="vehicleType"
              selected={props.value}
              select={(value) => {
                props.onChange(value);
                const newData = {
                  ...data,
                  vehicleType: value,
                };
                setData(newData);
              }}
              onBlur={props.onBlur}
              optionKey="name"
              option={vechicleData}
              t={t}
              placeholder={t("DEONAR_VEHICLE_TYPE")}
            />
          )}
        />
      </LabelFieldPair>
    // </div>
  );
};

export default VehicleTypeDropdownField;
