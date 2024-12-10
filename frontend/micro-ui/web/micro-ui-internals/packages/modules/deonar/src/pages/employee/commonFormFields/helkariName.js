import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import { helkariNameOptions } from "../../../constants/dummyData";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";

const HelkariNameField = ({ control, setData, data }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");
  const [options, setOptions] = useState([]);

  const { searchDeonarCommon } = useDeonarCommon();
  const useFetchOptions = (optionType) => {
    const { data } = searchDeonarCommon({
      CommonSearchCriteria: {
        Option: optionType,
      },
    });
    return data
      ? data.CommonDetails.map((item) => ({
          name: item.name,
          value: item.id,
        }))
      : [];
  };

  const helkariData = useFetchOptions("helkari");

  useEffect(() => {
    if (!data.helkariName) {
      setError(t("REQUIRED_FIELD"));
    } else {
      setError("");
    }
  }, [data]);

  useEffect(() => {
    setOptions(helkariData.map((item) => item.name || []));
  }, []);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_HELKARI_NAME")}</CardLabel>
        <Controller
          control={control}
          name="helkariName"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={(props) => (
            <div>
              <Dropdown
                name="helkariName"
                selected={props.value}
                select={(value) => {
                  props.onChange(value);
                  const newData = {
                    ...data,
                    helkariName: value,
                  };
                  setData(newData);
                }}
                onBlur={props.onBlur}
                optionKey="name"
                option={helkariData}
                t={t}
                placeholder={t("DEONAR_HELKARI_NAME")}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default HelkariNameField;
