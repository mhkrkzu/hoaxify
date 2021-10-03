import React from 'react';
import HomePage from "../pages/HomePage";
import LanguageSelector from "../components/LanguageSelector";
import {HashRouter as Router, Redirect, Route, Switch} from "react-router-dom";
import LoginPage from "../pages/LoginPage";
import UserSignupPage from "../pages/UserSignupPage";
import UserPage from "../pages/UserPage";
import TopBar from "../components/TopBar";
import {useSelector} from "react-redux";


const App = () => {

    const {isLoggedIn} = useSelector((store) => ({
        isLoggedIn: store.isLoggedIn
    }))

    return (
        <div>
            <Router>
                <TopBar/>
                <Switch>
                    <Route exact path='/' component={HomePage}/>
                    {!isLoggedIn && <Route path='/login' component={LoginPage}/>}
                    <Route path='/signup' component={UserSignupPage}/>
                    <Route path='/user/:username' component={UserPage}/>
                    }}/>
                    <Redirect to='/'/>
                </Switch>
            </Router>
            <LanguageSelector/>
        </div>
    );
}

export default App;
