import React, { useEffect, useState } from 'react';
import { Controller } from 'react-hook-form';
import { TextInput, LabelFieldPair, CardLabel } from '@upyog/digit-ui-react-components'; // Adjust according to your imports
import { useTranslation } from 'react-i18next';

const GenericFormInput = ({
  control,
  name,
  rules,
  placeholder,
  fetchData, // Function to fetch data from API
  readOnly = false,
  customValidation,
}) => {
  const { t } = useTranslation();
  const [apiValue, setApiValue] = useState("");

  useEffect(() => {
    if (fetchData) {
      fetchData()
        .then((response) => setApiValue(response))
        .catch((error) => console.error("API Error:", error));
    }
  }, [fetchData]);

  return (
    <div className="bmc-col3-card" style={{ width: "100%" }}>
      <LabelFieldPair>
        <CardLabel>{placeholder}</CardLabel>
        <Controller
          control={control}
          name={name}
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={({
            field = {}, // Provide default fallback for the entire field object
            field: { value = "", onChange, onBlur } = {}, // Provide fallback values for field properties
            fieldState: { error } = {}, // Ensure fieldState has a fallback
          }) => (
            <div>
              <TextInput
                type="text"
                value={apiValue || value || ""} // Populate from API or manual input
                onChange={(e) => {
                    onChange(e.target.value); // Call the form control's onChange
                    fetchData && fetchData(); // Fetch the license number or other data
                  }}
                onBlur={onBlur}
                readOnly={readOnly}
                placeholder={placeholder}
              />
              {error && <div style={{ color: 'red' }}>{error.message}</div>}
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default GenericFormInput;
