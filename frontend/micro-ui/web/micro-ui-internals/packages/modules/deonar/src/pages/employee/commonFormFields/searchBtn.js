import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const SearchButtonField = ({ onSearch, disabled,customStyle }) => {
  const { t } = useTranslation();

  return (
    <div className="bmc-col3-card">
      <div className="bmc-search-button" style={{ ...(customStyle ? {} : { textAlign: "end" }), paddingTop:customStyle ? "0px":"21px" }}>
        <button
          type="button"
          className="bmc-card-button"
          style={{ borderBottom: "3px solid black", backgroundColor: disabled ? "gray" : "#f47738", cursor: disabled ? "not-allowed" : "pointer" ,height:customStyle ? "33px":"auto"}}
          onClick={onSearch}
        >
          {t("Deonar_Search")}
        </button>
      </div>
    </div>
  );
};

export default SearchButtonField;
