import React, { useState } from "react";
import PropTypes from "prop-types";
import { NotificationBell } from "./svgindex";

const OpenLinkContainer = ({ img,}) => {
  return (
    <div className="navbar">
      <div className="center-container">
        <img
          className="city"
          id="topbar-logo" 
          crossOrigin="anonymous"
          src={"https://gambia-terraform.s3.ap-south-1.amazonaws.com/mSeva+BMC+logo.png"}
          alt="mSeva"
        />
      </div>
    </div>
  );
};

OpenLinkContainer.propTypes = {
  img: PropTypes.string,
};

OpenLinkContainer.defaultProps = {
  img: undefined,
};

export default OpenLinkContainer;