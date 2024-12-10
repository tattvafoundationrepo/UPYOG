import { ActionBar, Loader, Menu, SubmitBar, Toast } from "@upyog/digit-ui-react-components";
import React, { useCallback, useMemo, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import WorkflowPopup from "./Workflowpopup";

const WorkflowActions = ({
  businessService,
  tenantId,
  applicationNo: propApplicationNo,
  forcedActionPrefix,
  ActionBarStyle = {},
  MenuStyle = {},
  applicationDetails,
  url,
  setStateChanged,
  moduleCode,
  editApplicationNumber,
  editCallback,
  callback,
}) => {
  const history = useHistory();
  const { estimateNumber } = Digit.Hooks.useQueryParams();
  const { t } = useTranslation();

  const applicationNo = useMemo(() => propApplicationNo || estimateNumber, [propApplicationNo, estimateNumber]);
  const { mutate } = Digit.Hooks.bmc.useVerifierScheme();
  const [displayMenu, setDisplayMenu] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [selectedAction, setSelectedAction] = useState(null);
  const [isEnableLoader, setIsEnableLoader] = useState(false);
  const [showToast, setShowToast] = useState(null);
  const menuRef = useRef();

  const user = useMemo(() => Digit.UserService.getUser(), []);
  const userRoles = useMemo(() => user?.info?.roles?.map((e) => e.code), [user]);
  const workflowDetails = Digit.Hooks.useWorkflowDetailsV2({
    tenantId,
    id: applicationNo,
    moduleCode: businessService,
    config: { enabled: true, cacheTime: 0 },
  });

  const actions = useMemo(
    () =>
      workflowDetails?.data?.actionState?.nextActions?.filter((e) => userRoles.some((role) => e.roles?.includes(role)) || !e.roles) ||
      workflowDetails?.data?.nextActions?.filter((e) => userRoles.some((role) => e.roles?.includes(role)) || !e.roles),
    [workflowDetails, userRoles]
  );

  const closeMenu = useCallback(() => setDisplayMenu(false), []);

  const closeToast = useCallback(() => {
    setTimeout(() => setShowToast(null), 5000);
  }, []);

  Digit.Hooks.useClickOutside(menuRef, closeMenu, displayMenu);

  const closeModal = useCallback(() => {
    setSelectedAction(null);
    setShowModal(false);
    setShowToast({ warning: true, label: `WF_ACTION_CANCELLED` });
    closeToast();
  }, [closeToast]);

  const onActionSelect = useCallback((action) => {
    setDisplayMenu(false);
    setSelectedAction(action);
    setShowModal(true);
  }, []);

  const submitAction = useCallback(
    (data, selectedAction) => {
      setShowModal(false);
      setIsEnableLoader(true);
      const mydata = {
        ApplicationStatus: {
          action: selectedAction.action,
          Comment: data.VerificationRemarks,
          ApplicationNumbers: [applicationNo],
        },
      };
      const mutateObj = { ...data, selectedAction };
      mutate(mydata, {
        onSuccess: (data, variables) => {
          setIsEnableLoader(false);
          setShowToast({ label: Digit.Utils.locale.getTransformedLocale(`WF_UPDATE_SUCCESS_${businessService}_${selectedAction.action}`) });
          callback?.onSuccess?.();
          workflowDetails.revalidate();

          console.log("selectedAction.action", selectedAction.action);

          if (selectedAction.action === "APPROVE") {
            history.push("/digit-ui/employee/bmc/approve"); // Navigate to the approval page
          } else if (selectedAction.action === "VERIFY") {
            history.push("/digit-ui/employee/bmc/aadhaarverify"); // Navigate to the verification page
          } else if (selectedAction.action === "RANDOMIZE") {
            history.push("/digit-ui/employee/bmc/aadhaarverify"); // Navigate to the randomization page
          }
        },
        onError: (error, variables) => {
          setIsEnableLoader(false);
          setShowToast({
            error: true,
            label: Digit.Utils.locale.getTransformedLocale(`WF_UPDATE_ERROR_${businessService}_${selectedAction.action}`),
            isDleteBtn: true,
          });
          callback?.onError?.();
        },
      });
    },
    [businessService, callback, mutate, workflowDetails, applicationNo, history]
  );

  if (isEnableLoader) return <Loader />;

  return (
    <React.Fragment>
      {!workflowDetails?.isLoading && actions?.length > 0 && (
        <ActionBar style={{ ...ActionBarStyle }}>
          {displayMenu ? (
            <Menu
              localeKeyPrefix={forcedActionPrefix || Digit.Utils.locale.getTransformedLocale(`WF_${businessService?.toUpperCase()}_ACTION`)}
              options={actions}
              optionKey={"action"}
              t={t}
              onSelect={onActionSelect}
              style={MenuStyle}
            />
          ) : null}
          <SubmitBar ref={menuRef} label={t("WORKS_ACTIONS")} onSubmit={() => setDisplayMenu(!displayMenu)} />
        </ActionBar>
      )}
      {showModal && (
        <WorkflowPopup
          action={selectedAction}
          tenantId={tenantId}
          t={t}
          closeModal={closeModal}
          submitAction={submitAction}
          businessService={businessService}
          moduleCode={moduleCode}
          applicationDetails={applicationDetails}
        />
      )}
      {showToast && (
        <Toast
          error={showToast?.error}
          warning={t(showToast?.warning)}
          label={t(showToast?.label)}
          onClose={() => setShowToast(null)}
          isDleteBtn={showToast?.isDleteBtn}
        />
      )}
    </React.Fragment>
  );
};

export default WorkflowActions;
