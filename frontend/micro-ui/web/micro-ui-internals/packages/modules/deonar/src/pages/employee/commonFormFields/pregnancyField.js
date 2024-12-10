import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker, RadioButtons } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import { pregnancy } from "../../../constants/dummyData";

const PregnancyField = ({control, data, setData}) => {
  const { t } = useTranslation();
  const [pregnancyOptions, setPregnancyOptions] = useState([]);
  const [isActive, setIsactive] = useState({});

  useEffect(() => {
    setPregnancyOptions(pregnancy);
    setIsactive(pregnancyOptions[1]);
  }, []);

  return (
    <div className="bmc-col3-card">
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t("DEONAR_PREGNANCY")}</CardLabel>
            <Controller
            control={control}
            name="pregnancy"
            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
            render={(props) => (
                <div>
                <RadioButtons
                onSelect={(e) => {
                  props.onSelect(e);
                  // setIsactive(e);
                  const newData = {
                    ...data,
                    pregnancy: e
                  };
                  setData(newData);
                }}
                // selected={isActive}
                // selectedOption={isActive}
                onBlur={props.onBlur}
                optionsKey="name"
                options={pregnancyOptions}
              />
                </div>
            )}
            />
            
        </LabelFieldPair>
    </div>
  );
};

export default PregnancyField;
