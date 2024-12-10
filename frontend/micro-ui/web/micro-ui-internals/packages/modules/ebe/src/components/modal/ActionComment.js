import { CardLabel, Dropdown, TextInput } from "@upyog/digit-ui-react-components";
import React, { useState, useEffect } from 'react';
import { useTranslation } from "react-i18next";

const ActionComment = ({ config, onSelect, formData = {}, userType, register, errors }) => {
    const { t } = useTranslation();
    const reasonOptions = [
        { value: "Document not Available" },
        { value: "Incorrect Information" },
        { value: "Others" },
    ];
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
        <div>
            <div>
                <CardLabel className="bmc-label">{t("WF_REMARK")}</CardLabel>
                <div>
                    <Dropdown
                        selected={selectedReason}
                        option={reasonOptions}
                        select={SelectReason}
                        optionKey="value"
                        defaultValue={undefined}
                        t={t}
                    />
                </div>
            </div>
            <div>
                <CardLabel className="bmc-label">{t("WF_ADDITIONAL_COMMENT")}</CardLabel>
                <TextInput name={"comments"}
                    value={formData && formData[config.key] ? formData[config.key]["comments"] : undefined}
                    onChange={(e) => setValue(e.target.value, "comments")}
                />
            </div>
        </div>
    );
};

export default ActionComment;