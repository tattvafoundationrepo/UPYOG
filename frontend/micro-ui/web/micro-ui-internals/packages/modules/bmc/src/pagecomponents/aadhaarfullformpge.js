import React, { useCallback, useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import AddressDetailCard from "../components/AddressDetails";
import BankDetails from "../components/BankDetails";
import Timeline from "../components/bmcTimeline";
import DisabilityCard from "../components/DisabilityCard";
import PersonalDetailCard from "../components/PersonalDetails";
import QualificationCard from "../components/QualificationCard";
import Title from "../components/title";
import { Toast } from "@upyog/digit-ui-react-components";
import DocumentCard from "../components/DocumentCard";
import UserOthersDetails from "../components/userOthersDetails";
import { useQueryClient } from "react-query";

const AadhaarFullFormPage = () => {
  const tenantId = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.code || Digit.ULBService.getCurrentTenantId();
  const userDetails = Digit.UserService.getUser();
  const { t } = useTranslation();
  const history = useHistory();
  const [userDetail, setUserDetail] = useState({});
  const [updatedPersonalDetails, setupdatedPersonalDetails] = useState({});
  const [updatedQualifications, setupdatedQualifications] = useState([]);
  const [updatedDisability, setupdatedDisability] = useState({});
  const [updatedAddress, setupdatedAddress] = useState({});
  const [updatedBank, setupdatedBank] = useState([]);
  const [updatedDocument, setupdatedDocument] = useState([]);
  const [updateOthersDetails, setupdatedOthersDetails] = useState({});
  const [isButtonDisabled, setIsButtonDisabled] = useState(true);
  const [toast, setToast] = useState(null);
  const [qualificationLength, setQualificationLength] = useState()
  const [bankLength, setBankLength] = useState()
  const [documentLength, setDocumentLength] = useState()


  useEffect(() => {
    if (userDetail?.UserQualification?.length <= 1) {
      if (userDetail?.UserQualification[0]?.percentage === 0) {
        setQualificationLength(0);
      } else {
        setQualificationLength(userDetail?.UserQualification?.length);
      }
    } else {
      setQualificationLength(userDetail?.UserQualification?.length);
        console.log("user qualification found");
    }
    if(userDetail?.UserBank?.length <= 1){
      if(userDetail?.UserBank[0]?.branchId === 0){
        setBankLength(0);
      } else {
        setBankLength(userDetail?.UserBank?.length);
      }
    }else {
      setBankLength(userDetail?.UserBank?.length);
    }
    if(userDetail?.documentDetails?.length <= 1){
      if(userDetail?.documentDetails[0]?.available === false){
        setDocumentLength(0);
      } else {
        setDocumentLength(userDetail?.documentDetails?.length);
      }
    } else {
      setDocumentLength(userDetail?.documentDetails?.length);
    }
  }, [userDetail]);
  

  const userFunction = (data) => {
    if (data && data.UserDetails && data.UserDetails.length > 0) {
      const user = data.UserDetails[0];
      setUserDetail(user);
    }
  };

  const getUserDetails = { UserSearchCriteria: { Option: "full", TenantID: tenantId, UserID: userDetails?.info?.id } };
  Digit.Hooks.bmc.useUsersDetails(getUserDetails, { select: userFunction });

  const showToast = (type, message, duration = 5000) => {
    setToast({ key: type, action: message });
    setTimeout(() => {
      setToast(null);
    }, duration);
  };

  const validateForm = () => {
    const requiredFields = [
      updatedPersonalDetails.aadharName,
      updatedPersonalDetails.father,
      updatedPersonalDetails.dob,
      updatedPersonalDetails.aadharRef,
      updatedAddress.house,
      updatedAddress.landmark,
      updatedAddress.locality,
      updatedAddress.subDistrict,
      updatedAddress.state,
      updatedAddress.district,
      updatedAddress.street,
      updatedAddress.city,
      updatedBank,
    ];
    const isFormValid = requiredFields.every((field) => field !== null && field !== "" && field !== undefined);
    const hasRequiredDetails = qualificationLength > 0 && bankLength > 0 && documentLength > 0;
    setIsButtonDisabled(!(isFormValid && hasRequiredDetails));
  };

  const handleQualificationsUpdate = (updatedQualifications) => {
    setupdatedQualifications(updatedQualifications);
    window.location.reload();
    
  };

  const handlePersonalDetailUpdate = useCallback((updatedPersonalDetails) => {
    setupdatedPersonalDetails(updatedPersonalDetails);
    validateForm();
  }, []);

  const handleDisabilityUpdate = useCallback((updatedDisability) => {
    setupdatedDisability(updatedDisability);
    validateForm();
  }, []);


  const handleBankUpdate = useCallback((updatedBank) => {
    setupdatedBank(updatedBank);
    window.location.reload();

  }, []);

  const handleAddressUpdate = useCallback((updatedAddress) => {
    setupdatedAddress(updatedAddress);
    validateForm();
  }, []);

  const handleDocumentUpdate = useCallback((updatedDocument) => {
    setupdatedDocument(updatedDocument);
    window.location.reload();
  }, []);

  const handleOtherDetailsUpdate = useCallback((updateOthersDetails) => {
    setupdatedOthersDetails(updateOthersDetails);
    validateForm();
  }, []);

  const saveUserDetail = Digit.Hooks.bmc.useSaveUserDetail();

  const goNext = useCallback(() => {
    const data = {
      updatedAddress,
      updatedDisability,
      updatedQualifications,
      updatedPersonalDetails,
      updatedBank,
      updatedDocument,
      updateOthersDetails,
    };

    saveUserDetail.mutate(data, {
      onSuccess: () => {
        showToast("success", t("BMC_USER_SAVED_SUCCESSFULLY"));
        setTimeout(() => {
          history.push("/digit-ui/citizen/bmc/selectScheme");
        }, 1000);
      },
      onError: (error) => {
        console.error("Failed to save user details:", error);
        showToast("error", t("BMC_USER_SAVED_ERROR"));
      },
    });
  }, [
    updatedAddress,
    updatedDisability,
    updatedQualifications,
    updatedPersonalDetails,
    updatedBank,
    saveUserDetail,
    updatedDocument,
    updateOthersDetails,
    history,
    t,
  ]);

  useEffect(() => {
    validateForm();
  }, [
    updatedPersonalDetails,
    updatedQualifications,
    updatedDisability,
    updatedAddress,
    updatedBank,
    updatedDocument,
    updateOthersDetails,
     
    
  ]);

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        {window.location.href.includes("/citizen") ? <Timeline currentStep={2} /> : null}
        <Title text={t("Applicant Details")} />
        <PersonalDetailCard onUpdate={handlePersonalDetailUpdate} initialRows={userDetail} tenantId={tenantId} AllowEdit={true} />
        <AddressDetailCard onUpdate={handleAddressUpdate} initialRows={userDetail} tenantId={tenantId} AllowEdit={true} />
        <DisabilityCard onUpdate={handleDisabilityUpdate} initialRows={userDetail.divyang} tenantId={tenantId} AllowEdit={true} />
        <DocumentCard onUpdate={handleDocumentUpdate} initialRows={userDetail.documentDetails} tenantId={tenantId} AllowEdit={true}></DocumentCard>
        <UserOthersDetails onUpdate={handleOtherDetailsUpdate} initialRows={userDetail.UserOtherDetails} tenantId={tenantId} AllowEdit={true} />
        <QualificationCard
          onUpdate={handleQualificationsUpdate}
          initialRows={userDetail.UserQualification || []}
          tenantId={tenantId}
          AddOption={true}
          AllowRemove={true}
        />
        <BankDetails initialRows={userDetail.UserBank || []} tenantId={tenantId} AddOption={true} AllowRemove={true} onUpdate={handleBankUpdate} />
        <div className="bmc-card-row" style={{ paddingBottom: "1rem", display: "flex", gap: "10px", justifyContent: "end" }}>
          <button
            className="bmc-card-button-cancel"
            onClick={() => history.push("/digit-ui/citizen/bmc/bmc-home")}
            style={{ borderBottom: "3px solid black", outline: "none" }}
          >
            {t("BMC_Back to Home")}
          </button>
          <button
            className="bmc-card-button"
            onClick={goNext}
            style={{
              borderBottom: "3px solid black",
              marginRight: "1rem",
              backgroundColor: isButtonDisabled ? "grey" : "#f47738",
              cursor: isButtonDisabled ? "not-allowed" : "pointer",
            }}
            disabled={isButtonDisabled}
          >
            {t("BMC_Confirm")}
          </button>
        </div>
      </div>
      {toast && (
        <Toast
          error={toast.key === "error"}
          label={t(toast.key === "success" ? "BMC_USER_SAVED_SUCCESSFULLY" : toast.action)}
          onClose={() => setToast(null)}
          style={{ maxWidth: "670px" }}
        />
      )}
    </React.Fragment>
  );
};

export default AadhaarFullFormPage;
