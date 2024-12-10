// import React, { useState } from "react";
// import { useTranslation } from "react-i18next";
// import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
// import { Controller, useForm } from "react-hook-form";

// const SubmitButtonField = () => {
//   const { t } = useTranslation();

//   return (
//     <div className="bmc-card-row">
//         <div style={{ float: "right", paddingBottom: "1rem", textAlign: "end" }}>
//         <button type="submit" className="bmc-card-button" style={{ marginRight: "1rem", borderBottom: "3px solid black" }}>
//             {t("BMC_SAVE")}
//         </button>
//         </div>
//     </div>
//   );
// };

// export default SubmitButtonField;

import React from "react";
import { useTranslation } from "react-i18next";

const SubmitButtonField = ({ disabled }) => {
  const { t } = useTranslation();

  return (
    <div className="bmc-card-row">
      <div style={{ float: "right", paddingBottom: "1rem", textAlign: "end" }}>
        <button
          type="submit"
          className="bmc-card-button"
          disabled={disabled}
          style={{
            marginRight: "1rem",
            borderBottom: "3px solid black",
            backgroundColor: disabled ? "gray" : "#f47738",
            cursor: disabled ? "not-allowed" : "pointer",
          }}
        >
          {t("BMC_SAVE")}
        </button>
      </div>
    </div>
  );
};

export default SubmitButtonField;
