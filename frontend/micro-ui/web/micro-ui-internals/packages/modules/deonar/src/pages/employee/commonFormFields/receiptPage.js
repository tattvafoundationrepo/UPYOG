// ReceiptCard.js
import React from "react";
import { useTranslation } from "react-i18next";
import PropTypes from "prop-types";
import { Controller } from "react-hook-form";
import { TextInput, Dropdown } from "@upyog/digit-ui-react-components";

const ReceiptCard = ({ title, fields, control, values }) => {
  const { t } = useTranslation();

  return (
    <div
      style={{
        border: "2px dotted #ccc",
        padding: "20px",
        borderRadius: "10px",
        width: "100%",
        marginBottom: "20px",
      }}
    >
      <h3 style={{ fontSize: "18px", marginBottom: "20px" }}>{t(title)}</h3>
      {fields.map((field, index) => (
        <div key={index} style={{ marginBottom: "15px" }}>
          {field.type === "input" && (
            <Controller
              name={field.name}
              control={control}
              defaultValue={values[field.name] || ""}
              render={({ field }) => (
                <TextInput
                  {...field}
                  label={t(field.label)}
                  value={field.value}
                  required={field.required}
                  error={!!field.error}
                />
              )}
            />
          )}
          {field.type === "dropdown" && (
            <Controller
              name={field.name}
              control={control}
              defaultValue={values[field.name] || ""}
              render={({ field }) => (
                <Dropdown
                  {...field}
                  label={t(field.label)}
                  value={field.value}
                  required={field.required}
                  option={field.options}
                />
              )}
            />
          )}
        </div>
      ))}
    </div>
  );
};

ReceiptCard.propTypes = {
  title: PropTypes.string.isRequired,
  fields: PropTypes.arrayOf(
    PropTypes.shape({
      type: PropTypes.string,
      label: PropTypes.string,
      name: PropTypes.string,
      required: PropTypes.bool,
      options: PropTypes.array,
    })
  ).isRequired,
  control: PropTypes.object.isRequired,
  values: PropTypes.object.isRequired,
};

export default ReceiptCard;
