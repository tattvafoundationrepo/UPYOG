import React from 'react';
import { CloseSvg, HeaderBar, ButtonSelector, Toast, ActionBar, SubmitBar } from "@upyog/digit-ui-react-components";
//import PopUp from "../atoms/PopUp";
const CustomModal = ({ 
  isOpen, 
  onClose, 
  children, 
  title, 
  headerBarMain,
  headerBarEnd,
  popupStyles,
  actionCancelLabel,
  actionCancelOnSubmit,
  actionSaveLabel,
  actionSaveOnSubmit,
  actionSingleLabel,
  actionSingleSubmit,
  error,
  setError,
  formId,
  isDisabled,
  hideSubmit,
  style = {},
  popupModuleMianStyles,
  headerBarMainStyle,
  isOBPSFlow = false,
  popupModuleActionBarStyles = {}, }) => {
  if (!isOpen) return null;
  const mobileView = Digit.Utils.browser.isMobile() ? true : false;
  return (
    <div className="deonar-modal-overlay" onClick={onClose}>
      <div className="deonar-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="deonar-modal-header">
          {title}
          <div style={{ backgroundColor: "rgb(204, 204, 204)", padding: "10px", borderRadius: "8px", cursor: "pointer" }}>
            <CloseSvg onClick={onClose} fill="#505A5F" />
          </div>
        </div>
        <div className="deonar-modal-body">
          {children}
        </div>
        <div className="popup-module-action-bar">
          {actionCancelLabel ? <ButtonSelector theme="border" label={actionCancelLabel} onSubmit={actionCancelOnSubmit} style={style} /> : null}
          {!hideSubmit ? (
            <ButtonSelector label={actionSaveLabel} onSubmit={actionSaveOnSubmit} formId={formId} isDisabled={isDisabled} style={style} />
          ) : null}
          {actionSingleLabel ?
            <ActionBar style={{ position: mobileView ? "absolute" : "relative", boxShadow: "none", minWidth: "240px", maxWidth: "360px", margin: "16px" }}>
              <div style={{ width: "100%" }}>
                <SubmitBar style={{ width: "100%" }} label={actionSingleLabel} onSubmit={actionSingleSubmit} />
              </div>
            </ActionBar> : null}
        </div>
      </div>
    </div>
  );
};

export default CustomModal;
