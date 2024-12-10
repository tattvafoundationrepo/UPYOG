import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import { shadeNumberOptions } from "../../../constants/dummyData";

const ShadeNumberField = ({control, setData, data, style}) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");
  const [options, setOptions] = useState([]);

  useEffect(() => {
    if (!data.shadeNumber) {
      setError("*");
    }
    else {
      setError("");
    }
  }, [data]);

  useEffect(() => {
    setOptions(shadeNumberOptions);
  }, []);

  return (
    <div className="bmc-col3-card" style={style}>
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t("DEONAR_SHADE_NUMBER")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
            <Controller
                control={control}
                name="shadeNumber"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                <div>
                    <Dropdown
                    selected={props.value}
                    select={(value) => {
                      props.onChange(value);
                      const newData = {
                        ...data,
                        shadeNumber: value
                      };
                      setData(newData);
                    }}
                    onBlur={props.onBlur}
                    optionKey="name"
                    option={options}
                    t={t}
                    placeholder={t("DEONAR_SHADE_NUMBER")}
                    />
                </div>
                )}
            />
        </LabelFieldPair>
      </div>
  );
};

export default ShadeNumberField;
