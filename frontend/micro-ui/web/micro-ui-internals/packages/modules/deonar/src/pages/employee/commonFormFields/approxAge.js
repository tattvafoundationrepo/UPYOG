// import React, { useEffect, useState } from "react";
// import { useTranslation } from "react-i18next";
// import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
// import { Controller } from "react-hook-form";

// const ApproximateAgeField = ({ control, data, setData }) => {
//   const { t } = useTranslation();
//   const [error, setError] = useState("");

//   const handleInputChange = (e, props) => {
//     const value = e.target.value.replace(/\D/g, ""); // Ensure only numeric input
//     const numericValue = parseInt(value, 10);

//     if (numericValue > 0 && numericValue < 50) {
//       setError("");
//       props.onChange(numericValue);
//       const newData = {
//         ...data,
//         approximateAge: numericValue,
//       };
//       setData(newData);
//     } else {
//       setError(t("INVALID_AGE_VALUE"));
//       props.onChange(""); // Clear the value if it's invalid
//       setData({ ...data, approximateAge: "" });
//     }
//   };

//   return (
//     <div className="bmc-col3-card">
//       <LabelFieldPair>
//         <CardLabel className="bmc-label">{t("DEONAR_APPROXIMATE_AGE")}</CardLabel>
//         <Controller
//           control={control}
//           name="approximateAge"
//           rules={{
//             required: t("CORE_COMMON_REQUIRED_ERRMSG"),
//             validate: value => value > 0 && value < 50 || t("AGE_MUST_BE_BETWEEN_1_AND_49")
//           }}
//           render={(props) => (
//             <div>
//               <TextInput
//                 type="number"
//                 value={props.value}
//                 onChange={(e) => handleInputChange(e, props)}
//                 onBlur={props.onBlur}
//                 optionKey="i18nKey"
//                 t={t}
//                 placeholder={t("DEONAR_APPROXIMATE_AGE")}
//               />
//               {props.error && <div style={{ color: "red" }}>{props.error.message}</div>}
//             </div>
//           )}
//         />
//       </LabelFieldPair>
//       {error && <div style={{ color: "red" }}>{error}</div>}
//     </div>
//   );
// };

// export default ApproximateAgeField;

import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const ApproximateAgeField = ({ label, name, control, setData, data, options, required }) => {
  const { t } = useTranslation();

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
              <Dropdown
                value={props.value}
                name={name}
                selected={props.value}
                select={(value) => {
                  props.onChange(value);
                  const newData = {
                    ...data,
                    [name]: value,
                  };
                  setData(newData);
                }}
                onBlur={props.onBlur}
                optionKey="name"
                option={options}
                t={t}
                placeholder={t(label)}
                required={required || false}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default ApproximateAgeField;
