import React, { useState, useEffect } from "react";
import { Controller } from "react-hook-form";
import { LabelFieldPair, CardLabel, Dropdown } from "@upyog/digit-ui-react-components";

const OpinionDropdown = ({ control, t, localData, handleChange, opinionOptions }) => {
  const [opinionNameToId, setOpinionNameToId] = useState({});

  useEffect(() => {
    if (opinionOptions?.length) {
      const opinionMap = opinionOptions.reduce((acc, item) => {
        if (item?.name && item?.value) {
          acc[item.name] = item.value;
        }
        return acc;
      }, {});
      setOpinionNameToId(opinionMap);
    }
  }, []);

  // Handle selection changes
  const handleSelectionChange = (selectedName) => {
    const selectedId = opinionNameToId[selectedName];
    handleChange("opinion", selectedName);
    control.setValue("opinionId", selectedId, { shouldDirty: true });
    handleChange("opinionId", selectedId);
  };

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_OPINION")}</CardLabel>
        <Controller
          control={control}
          name="opinion"
          render={(props) => (
            <Dropdown
              option={opinionOptions.map((item) => item.name)}
              select={(selectedName) => {
                handleSelectionChange(selectedName);
                props.onChange(selectedName);
              }}
              selected={localData?.opinion}
              t={t}
              onBlur={props.onBlur}
              placeholder={t("DEONAR_OPINION")}
            />
          )}
        />
      </LabelFieldPair>

      <Controller control={control} name="opinionId" defaultValue="" render={() => null} />
    </div>
  );
};

export default OpinionDropdown;
