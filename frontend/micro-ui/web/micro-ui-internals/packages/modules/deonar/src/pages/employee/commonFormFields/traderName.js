import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, MobileNumber } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";
import useDeonarCommon from "../../../../../../libraries/src/hooks/deonar/useCommonDeonar";
import MultiColumnDropdown from "./multiColumnDropdown";

const TraderNameField = ({ control, data, setData, disabled, className, style }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");
  const [options, setOptions] = useState([]);
  const [selectedOption, setSelectedOption] = useState(data.traderName);

  const { fetchDeonarCommon, searchDeonarCommon } = useDeonarCommon();

  const { data: commonApiData } = fetchDeonarCommon({
    CommonSearchCriteria: {
      Option: "stakeholder",
    },
  });

  const { data: traderData, error: traderError, refetch } = searchDeonarCommon({
    CommonSearchCriteria: {
      Option: "trader",
    },
    enabled: false,
  });

  useEffect(() => {
    if (commonApiData && Array.isArray(commonApiData.CommonDetails)) {
      const stakeholders = commonApiData.CommonDetails.map((item) => ({
        name: item.name,
        value: item.id,
      }));
      setOptions(stakeholders);
    } else {
      setOptions([]);
    }
  }, [commonApiData]);

  useEffect(() => {
    if (traderData && Array.isArray(traderData.CommonDetails)) {
      const traderNames = traderData.CommonDetails.map((item) => ({
        name: item.name,
        value: item.id,
        licenceNumber: item.licenceNumber,
        mobileNumber: item.mobileNumber,
        tradertype: item.tradertype, 
        registrationnumber: item.registrationnumber
      }));
      setOptions(traderNames);
    }
  }, [traderData]);

  useEffect(() => {
    if (!data.traderName) {
      setError(t("*"));
    } else {
      setError("");
    }
  }, [data.traderName, t]);

  const handleSelectionChange = (selectedOption) => {
    setSelectedOption(selectedOption[0]);
    setData((prevData) => ({
      ...prevData,
      traderName: selectedOption,
    }));

    if (selectedOption) {
      refetch();
    }
  };

  const handleSelect = (e, selectedOptions) => {
    
    setSelectedOption(selectedOptions);  // Update the selectedOption state
  };

  return (
    <div className={`bmc-col3-card ${className}`} style={style}>
      <LabelFieldPair>
        <CardLabel className="bmc-label">
          {t("DEONAR_TRADER_NAME")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}
        </CardLabel>
        {/* <Controller
          control={control}
          name="traderName"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={({ onChange, onBlur, value }) => (
            <Dropdown
              selected={value}
              select={(selected) => {
                onChange(selected);
                handleSelectionChange(selected);
              }}
              onBlur={onBlur}
              optionKey="name"
              t={t}
              placeholder={'DEONAR_TRADER_NAME'}
              option={options}
              required={true}
              disabled={disabled}
            />
         
          )}
        /> */}
        <MultiColumnDropdown
          options={options}
          selected={selectedOption ? [selectedOption] : []}
          onSelect={handleSelectionChange}
          defaultLabel="DEONAR_TRADER_NAME"
          displayKeys={["name", "licenceNumber", "mobileNumber", "registrationnumber"]}
          optionsKey="name"
           defaultUnit="Options"
          autoCloseOnSelect={true}
          showColumnHeaders={true}
        />
      </LabelFieldPair>
    </div>
  );
};

export default TraderNameField;
