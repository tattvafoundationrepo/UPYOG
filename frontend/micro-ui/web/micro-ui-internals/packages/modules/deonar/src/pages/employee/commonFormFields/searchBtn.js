import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const SearchButtonField = ({ onSearch, disabled }) => {
  const { t } = useTranslation();

  return (
    <div className="bmc-col3-card">
      <div className="bmc-search-button" style={{ textAlign: "end", paddingTop: "21px" }}>
        <button
          type="button"
          className="bmc-card-button"
          style={{ borderBottom: "3px solid black", backgroundColor: disabled ? "gray" : "#f47738", cursor: disabled ? "not-allowed" : "pointer" }}
          onClick={onSearch}
        >
          {t("BMC_Search")}
        </button>
      </div>
    </div>
  );
};

export default SearchButtonField;
