import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import { daysOfWeek } from "../../../constants/dummyData";

const InspectionDate = ({label, name, control, setData, data, getValues, setValue}) => {
  const { t } = useTranslation();

    useEffect(() => {
    if (name === "anteMortemInspectionDate") {
      let date = getValues("anteMortemInspectionDate");
      let day = daysOfWeek[new Date(date).getDay()];
      console.log(day);
      setValue("anteMortemInspectionDay", day);
      const newData = {
        ...data,
        anteMortemInspectionDay: day
      };
      setData(newData);
    }
  }, []);

  return (
    <div className="bmc-col3-card">
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t(label)}</CardLabel>
            <Controller
            control={control}
            name={name}
            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
            render={(props) => (
                <div>
                  <DatePicker 
                  date={props.value} 
                  onChange={(e) => {
                    props.onChange(e);
                    let newData = {};
                    if (name === "anteMortemInspectionDate") {
                      newData = {
                        ...data,
                        anteMortemInspectionDay: daysOfWeek[new Date(e).getDay()],
                        anteMortemInspectionDate: e
                      };
                      let day = daysOfWeek[new Date(e).getDay()];
                      console.log(day);
                      setValue("anteMortemInspectionDay", day);
                    }
                    else if (name === "postMortemInspectionDate") {
                      newData = {
                        ...data,
                        postMortemInspectionDay: daysOfWeek[new Date(e).getDay()],
                        postMortemInspectionDate: e
                      };
                      let day = daysOfWeek[new Date(e).getDay()];
                      console.log(day);
                      setValue("postMortemInspectionDay", day);
                    }
                    else {
                      newData = {
                        ...data,
                        [name]: e
                      };
                    }
                    setData(newData);
                  }} 
                  onBlur={props.onBlur} 
                  placeholder={label} />
                </div>
            )}
            />
        </LabelFieldPair>
    </div>
  );
};

export default InspectionDate;
