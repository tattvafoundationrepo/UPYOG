import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";
import { dawanwalaNameOptions } from "../../../constants/dummyData";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";

const DawanwalaNameField = ({ control, data, setData, disabled , style}) => {
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
 
  const dawanwalaData = useFetchOptions("dawanwala");

  useEffect(() => {
    if (!data.dawanwalaName) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    } else {
      setError("");
    }
  }, [data, t]);

  useEffect(() => {
    setOptions(dawanwalaData);
  }, []);

  useEffect(() => {
    if (disabled) {
      setError("");
    }
  }, [disabled]);

  return (
    <div className="bmc-col3-card" style={style}>
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_DAWANWALA_NAME")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="dawanwalaName"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={(props) => (
            <div>
              <Dropdown
                name="dawanwalaName"
                selected={props.value}
                select={(value) => {
                  props.onChange(value);
                  const newData = {
                    ...data,
                    dawanwalaName: value
                  };
                  setData(newData);
                }}
                onBlur={props.onBlur}
                optionKey="name"
                option={options}
                t={t}
                placeholder={t("DEONAR_DAWANWALA_NAME")}
                disabled={disabled}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default DawanwalaNameField;
