import React, { useRef, useState, useEffect } from "react";
import PropTypes from "prop-types";
import { ArrowDown } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";

// A simple event bus to coordinate between multiple instances
const EventBus = {
  events: {},
  on(event, callback) {
    if (!this.events[event]) this.events[event] = [];
    this.events[event].push(callback);
  },
  off(event, callback) {
    if (!this.events[event]) return;
    this.events[event] = this.events[event].filter((cb) => cb !== callback);
  },
  emit(event, data) {
    if (!this.events[event]) return;
    this.events[event].forEach((callback) => callback(data));
  },
};

const MultiColumnDropdown = ({
  options = [],
  selected = [],
  onSelect,
  defaultUnit = "",
  BlockNumber = 1,
  disabled = false,
  displayKeys = [],
  optionsKey = "value",
  autoCloseOnSelect = true,
  showColumnHeaders = false,
  readOnly = false,
  name = "",
  headerMappings = {},
  placeholder = "Search Or Select...",
}) => {
  const [active, setActive] = useState(false);
  const [searchQuery, setSearchQuery] = useState(""); // Initialize as an empty string
  const [optionIndex, setOptionIndex] = useState(-1);
  const dropdownRef = useRef();
  const inputRef = useRef();
  const { t } = useTranslation();

  // Close other dropdowns when this one is activated
  const handleActivate = () => {
    setActive(true);
    EventBus.emit("closeAllDropdowns", dropdownRef);
  };

  // Listen to a global event to close this dropdown if needed
  useEffect(() => {
    const handleCloseAllDropdowns = (ref) => {
      if (ref !== dropdownRef) {
        setActive(false);
      }
    };

    EventBus.on("closeAllDropdowns", handleCloseAllDropdowns);

    // Clean up event listener
    return () => {
      EventBus.off("closeAllDropdowns", handleCloseAllDropdowns);
    };
  }, []);

  // Handle clicks outside of the dropdown to close it
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setActive(false); // Close the dropdown
        setSearchQuery(""); // Reset search query on outside click
      }
    };

    // Attach the event listener to the document
    document.addEventListener("mousedown", handleClickOutside);

    // Clean up the event listener
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [active]);

  // Close the dropdown when pressing Tab to navigate
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === "Tab" && active) {
        setActive(false);
        setSearchQuery(""); // Reset the search query when navigating away
      }
    };

    document.addEventListener("keydown", handleKeyDown);

    return () => {
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [active]);

  const filteredOptions = searchQuery
    ? options.filter((option) =>
        displayKeys.some((key) => (t(option[key]) || "").toLowerCase().includes(searchQuery.toLowerCase()))
      )
    : options;

  const onSearch = (e) => {
    setSearchQuery(e.target.value);
  };

  const handleSelect = (e, option) => {
    if (!disabled) {
      onSelect(e, [option]);
      if (autoCloseOnSelect) {
        setActive(false);
        setSearchQuery(""); // Reset search after selection
      }
    }
    e?.stopPropagation();
  };

  const getSelectedValue = () => {
    return selected.length > 0 ? selected[0].label : "";
  };

  const toggleDropdown = () => {
    if (active) {
      setActive(false);
    } else {
      handleActivate();
    }
  };

  return (
    <div className="multi-select-dropdown-wrap" ref={dropdownRef}>
      <div className={`master${active ? `-active` : ""}`}>
        <input
          className="cursorPointer"
          type="text"
          ref={inputRef}
          onFocus={() => {
            if (!active) {
              setActive(true); // Only activate if not already active
            }
          }}
          value={searchQuery || getSelectedValue() || undefined} // Use undefined to allow placeholder to show
          onChange={onSearch}
          disabled={disabled}
          readOnly={readOnly}
          placeholder={placeholder} // Properly handle placeholder visibility
          onBlur={() => {
            if (!searchQuery && !getSelectedValue()) {
              setSearchQuery(""); // Reset the search query if necessary
            }
          }}
        />
        <div className="label" onClick={toggleDropdown}>
          <p>{getSelectedValue()}</p>
          <ArrowDown />
        </div>
      </div>
      {active && !disabled && (
        <div className="server" id="jk-dropdown-unique">
          {showColumnHeaders && (
            <div
              className="column-headers"
              style={{ display: "grid", gridTemplateColumns: `repeat(${displayKeys.length}, 1fr)`, fontWeight: "bold" }}
            >
              {displayKeys.map((key) => (
                <p className="label" key={key}>
                  {headerMappings[key] || t(key)}
                </p>
              ))}
            </div>
          )}
          {filteredOptions.map((option, index) => (
            <div
              key={index}
              className={`menu-item ${index === optionIndex ? "active" : ""}`}
              style={{ display: "grid", gridTemplateColumns: `repeat(${displayKeys.length}, 1fr)` }}
              onClick={(e) => handleSelect(e, option)}
            >
              {displayKeys.map((key) => (
                <p className="label" key={key}>
                  {t(option[key]) || "N/A"}
                </p>
              ))}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

MultiColumnDropdown.propTypes = {
  options: PropTypes.array.isRequired,
  selected: PropTypes.array.isRequired,
  onSelect: PropTypes.func.isRequired,
  defaultUnit: PropTypes.string,
  BlockNumber: PropTypes.number,
  disabled: PropTypes.bool,
  displayKeys: PropTypes.array.isRequired,
  optionsKey: PropTypes.string.isRequired,
  autoCloseOnSelect: PropTypes.bool,
  showColumnHeaders: PropTypes.bool,
  readOnly: PropTypes.bool,
  headerMappings: PropTypes.object,
  name: PropTypes.string,
  placeholder: PropTypes.string,
};

export default MultiColumnDropdown;