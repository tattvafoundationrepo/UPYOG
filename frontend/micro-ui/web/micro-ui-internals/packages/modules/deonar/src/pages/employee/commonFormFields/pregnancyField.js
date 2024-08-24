import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker, RadioButtons } from "@egovernments/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import { pregnancy } from "../../../constants/dummyData";

const PregnancyField = ({control, data, setData}) => {
  const { t } = useTranslation();
  const [pregnancyOptions, setPregnancyOptions] = useState([]);
  const [isActive, setIsactive] = useState({});

  useEffect(() => {
    setPregnancyOptions(pregnancy);
  }, []);

  return (
    <div className="bmc-col3-card">
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t("DEONAR_PREGNANCY")}</CardLabel>
            <RadioButtons
                onSelect={(e) => {
                  setIsactive(e);
                  const newData = {
                    ...data,
                    pregnancy: e
                  };
                  setData(newData);
                }}
                selected={isActive}
                selectedOption={isActive}
                optionsKey="name"
                name="pregnancy"
                options={pregnancyOptions}
              />
        </LabelFieldPair>
    </div>
  );
};

export default PregnancyField;
