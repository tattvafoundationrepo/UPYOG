import { CardLabel, DatePicker, TextInput } from "@upyog/digit-ui-react-components";
import React, {useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { convertEpochToDate } from "../../utils/index";

const TestCard = ({ config, onSelect, formData = {}, userType, register, errors }) => {
  const { t } = useTranslation();
  const [selectedReason, setSelectedReason] = useState(formData?.SelectReason);
  useEffect(() => {
    onSelect(config.key, selectedReason);
  }, [selectedReason, config.key, onSelect]);

  function SelectReason(value) {
    setSelectedReason(value);
  }
  
  function setValue(value, input) {
    onSelect(config.key, { ...formData[config.key], [input]: value });
  }

  return (
    <React.Fragment>
      <div>
        <div>
          <CardLabel className="bmc-label">{t("EBE_EXTENSION_DAYS")}&nbsp;{errors.comments1 && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.comments1.message}</sup>}</CardLabel>
          <div>
            <TextInput name={"comments1"}
              value={formData && formData[config.key] ? formData[config.key]["comments1"] : undefined}
              onChange={(e) => setValue(e.target.value, "comments1")}
            />
          </div>
        </div>
      </div>
      <div>
        <div>
          <CardLabel className="bmc-label">{t("EBE_END_DATE")}&nbsp;{errors.name && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.name.message}</sup>}</CardLabel>
          <DatePicker
            key={"name"}
            min={new Date(2000, 0, 1)}
            max={convertEpochToDate(new Date())}
            date={formData && formData[config.key] ? formData[config.key]["name"] : undefined}
            onChange={(e) => setValue(e, "name")}
            disable={false}
            defaultValue={undefined}
          />
        </div>
      </div>
    </React.Fragment>
  );
};

export default TestCard;
