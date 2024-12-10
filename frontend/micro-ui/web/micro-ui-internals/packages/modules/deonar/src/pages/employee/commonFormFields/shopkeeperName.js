import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";

const ShopkeeperNameField = ({ control, data, setData, disabled, style }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");
  const [options, setOptions] = useState([]);

  const { fetchEntryFeeDetailsbyUUID, searchDeonarCommon } = useDeonarCommon();
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

  const shopkeeperData = useFetchOptions("shopkeeper");

  useEffect(() => {
    if (!data.shopkeeperName) {
      setError("REQUIRED_FIELD");
    } else {
      setError("");
    }
  }, [data]);

  useEffect(() => {
    setOptions(shopkeeperData.map((item) => item.name || []));
  }, []);

  console.log("shopkeeperData", shopkeeperData);

  return (
    <div className="bmc-col3-card" style={style}>
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_SHOPKEEPER_NAME")}</CardLabel>
        <Controller
          control={control}
          name="shopkeeperName"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={(props) => (
            <div>
              <Dropdown
                name="shopkeeperName"
                selected={props.value}
                select={(value) => {
                  props.onChange(value);
                  const newData = {
                    ...data,
                    shopkeeperName: value,
                  };
                  setData(newData);
                }}
                onBlur={props.onBlur}
                option={shopkeeperData}
                optionKey="name"
                t={t}
                placeholder={t("DEONAR_SHOPKEEPER_NAME")}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default ShopkeeperNameField;
