import React, { useState } from "react";
import { CardLabel, LabelFieldPair } from "@upyog/digit-ui-react-components";
import { useHistory } from "react-router-dom";
import Timeline from "../components/bmcTimeline";
import { useTranslation } from "react-i18next";
import OTPInput from "../components/OTPInput";

const AadhaarVerification = () => {
  const tenantId = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.code || Digit.ULBService.getCurrentTenantId();
  const [aadhaar, setAadhaar] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [existingAadhaarNumber, setExistingAadhaarNumber] = useState(false);
  const userDetails = Digit.UserService.getUser();
  const [userDetail, setUserDetail] = useState({});
  const history = useHistory();
  const { t } = useTranslation();

  const userFunction = (data) => {
    if (data && data.UserDetails && data.UserDetails.length > 0) {
      setUserDetail(data.UserDetails[0]);
      const existingAadhaar = data.UserDetails[0].AadharUser?.aadharRef;
      if (existingAadhaar) {
        setAadhaar(existingAadhaar);
        setExistingAadhaarNumber(true);
      } else {
        setExistingAadhaarNumber(false);
      }
    }
  };

  const getUserDetails = { UserSearchCriteria: { Option: "full", TenantID: tenantId, UserID: userDetails?.info?.id } };
  Digit.Hooks.bmc.useUsersDetails(getUserDetails, { select: userFunction });

  const validateAadhaar = (number) => {
    if (!number) {
      return t("Aadhaar number cannot be empty");
    }
    if (number.length !== 12 || !/^\d+$/.test(number)) {
      return t("Aadhaar number should contain exactly 12 digits");
    }
    return "";
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    if (isSubmitting) return;
    setIsSubmitting(true);

    const validationError = validateAadhaar(aadhaar);
    if (validationError) {
      setError(validationError);
      setIsSubmitting(false);
      return;
    }

    history.push({
      pathname: "/digit-ui/citizen/bmc/aadhaarForm",
      state: { aadharRef: aadhaar },
    });

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
                <div className="aadhaar-container">
                  <LabelFieldPair>
                    <CardLabel>{t("BMC_AADHAR_NUMBER")}</CardLabel>
                    <OTPInput length={12} value={aadhaar} onChange={handleAadhaarChange} disabled={existingAadhaarNumber} />
                  </LabelFieldPair>
                </div>
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
