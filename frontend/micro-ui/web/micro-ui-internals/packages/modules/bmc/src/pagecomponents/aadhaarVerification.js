import { CardLabel, LabelFieldPair, OTPInput } from "@upyog/digit-ui-react-components";
import React, { useState, useEffect } from "react";
import { useHistory } from "react-router-dom";
import Timeline from "../components/bmcTimeline";
import { useTranslation } from "react-i18next";

const AadhaarVerification = () => {
  const [aadhaar, setAadhaar] = useState("");
  const [error, setError] = useState("");
  const [isAadhaarValid, setIsAadhaarValid] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const history = useHistory();
  const { t } = useTranslation();

  useEffect(() => {
    if (aadhaar.length === 12 && /^[0-9]{12}$/.test(aadhaar)) {
      setIsAadhaarValid(true);
      setError("");
    } else {
      setIsAadhaarValid(false);
      if (aadhaar.length === 12) {
        setError(t("Aadhaar number should contain exactly 12 digits"));
      } else {
        setError("");
      }
    }
  }, [aadhaar, t]);

  const handleSubmit = (event) => {
    event.preventDefault();

    if (isSubmitting) return;
    setIsSubmitting(true);

    if (!aadhaar) {
      setError(t("Aadhaar number should contain exactly 12 digits"));
    } else if (isAadhaarValid) {
      history.push({
        pathname: "/digit-ui/citizen/bmc/aadhaarForm",
        state: { aadharRef: aadhaar },
      });
    }

    setIsSubmitting(false);
  };

  const handleAadhaarChange = (value) => {
    setAadhaar(value);
    setError("");
  };

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        {window.location.href.includes("/citizen") && <Timeline currentStep={1} />}
        <div className="bmc-row-card-header" style={{ padding: 0 }}>
          <div className="bmc-row-card-content" style={{ height: "80%" }}>
            <div className="bmc-col2-card" style={{ height: "49vh", display: "flex", justifyContent: "center", alignItems: "center" }}>
              <div className="bmc-aadhaarText">
                <div className="bmc-title" style={{ textAlign: "center" }}>
                  {t("BMC_AADHAAR_VERIFICATION")}
                </div>
                <LabelFieldPair>
                  <CardLabel className="aadhaar-label">{t("BMC_AADHAAR_LABEL")}</CardLabel>
                  <div className="aadhaar-container" style={{ width: "580px" }}>
                    <OTPInput length={12} value={aadhaar} onChange={handleAadhaarChange} />
                  </div>
                </LabelFieldPair>
                {error && <div style={{ textAlign: "center", color: "red" }}>{error}</div>}
                <div style={{ textAlign: "center" }}>
                  <button
                    type="submit"
                    className="bmc-card-button"
                    onClick={handleSubmit}
                    style={{ borderBottom: "3px solid black", textAlign: "center", outline: "none" }}
                  >
                    {t("BMC_SUBMIT")}
                  </button>
                </div>
              </div>
            </div>
            <div className="bmc-col2-card" style={{ padding: 0 }}>
              <div className="bmc-card-aadharimage" />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default AadhaarVerification;
